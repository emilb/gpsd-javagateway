#!/bin/bash -eu

#####################################################################
#
# touch osm-setup.sh; chmod +x osm-setup.sh; vi osm-setup.sh
# copy the contents of this file to osm-setup.sh and execute!

TILE_HOST="tile.panama.se"
TILE_CONTEXT="tiles"
#OSM_IMPORT_FILE="scandinavia.osm.bz2"
OSM_IMPORT_FILE="planet-latest.osm.bz2"

#####################################################################
#
# How to extract a bounding box from planet-latest.osm.bz2:
#
# Download the planet file:
# wget http://planet.openstreetmap.org/planet-latest.osm.bz2
#
# Download osmosis:
# wget http://dev.openstreetmap.org/~bretth/osmosis-build/osmosis-latest.tgz
#
# Untar:
# tar zxvf osmosis-latest.tgz
#
# Get the coordinates for your bounding box, in the example below Scandinavia
# will be extracted. The coordinates can be found using the export tab at 
# http://openstreetmap.org
# 
# Example: 
# http://www.openstreetmap.org/?minlon=3&minlat=52.9&maxlon=31.9&maxlat=71.5&box=yes
#
# Use osmosis to extract the bounding box:
#
# bzcat planet-latest.osm.bz2 | osmosis-0.39/bin/osmosis --read-xml\
# enableDateParsing=no file=-\
# --bounding-box top=71.5 left=3 bottom=52.9 right=31.9 --write-xml file=-\
# | bzip2 > extracted.osm.bz2
#
#####################################################################


#####################################################################
# Elapsed time.  Usage:
#
#   t=$(timer)
#   ... # do something
#   printf 'Elapsed time: %s\n' $(timer $t)
#      ===> Elapsed time: 0:01:12
#
#
#####################################################################
# If called with no arguments a new timer is returned.
# If called with arguments the first is used as a timer
# value and the elapsed time is returned in the form HH:MM:SS.
#
function timer()
{
    if [[ $# -eq 0 ]]; then
        echo $(date '+%s')
    else
        local  stime=$1
        etime=$(date '+%s')

        if [[ -z "$stime" ]]; then stime=$etime; fi

        dt=$((etime - stime))
        ds=$((dt % 60))
        dm=$(((dt / 60) % 60))
        dh=$((dt / 3600))
        printf '%d:%02d:%02d' $dh $dm $ds
    fi
}

t_setup=$(timer)

#####################################################################
# Disable DNS lookups on ssh login
#####################################################################
if ( grep 'UseDNS' /etc/ssh/sshd_config ); then
    echo "UseDNS already defined!"
else
    echo "Removing DNS check on ssh login"
    echo "UseDNS no" | sudo tee -a /etc/ssh/sshd_config
    sudo service ssh restart
fi

#####################################################################
# Install all needed packages and utilites
#####################################################################
echo "Installing general utilities..."
sudo apt-get -qq -y install subversion autoconf screen htop unzip emacs

echo "Installing postGiS database"
sudo apt-get -qq -y install postgresql-8.4-postgis postgresql-contrib-8.4
sudo apt-get -qq -y install postgresql-server-dev-8.4
sudo apt-get -qq -y install build-essential libxml2-dev libtool
sudo apt-get -qq -y install libgeos-dev libpq-dev libbz2-dev proj

echo "Installing dependencies for Mapnik"
sudo apt-get -qq -y install libltdl3-dev libpng12-dev libtiff4-dev libicu-dev
sudo apt-get -qq -y install libboost-python1.42-dev python-cairo-dev python-nose
sudo apt-get -qq -y install libboost1.42-dev libboost-filesystem1.42-dev
sudo apt-get -qq -y install libboost-iostreams1.42-dev libboost-regex1.42-dev libboost-thread1.42-dev
sudo apt-get -qq -y install libboost-program-options1.42-dev libboost-python1.42-dev
sudo apt-get -qq -y install libfreetype6-dev libcairo2-dev libcairomm-1.0-dev
sudo apt-get -qq -y install libgeotiff-dev libtiff4 libtiff4-dev libtiffxx0c2
sudo apt-get -qq -y install libsigc++-dev libsigc++0c2 libsigx-2.0-2 libsigx-2.0-dev
sudo apt-get -qq -y install libgdal1-dev python-gdal
sudo apt-get -qq -y install imagemagick ttf-dejavu

echo "Installing dependencies for mod_tile"
sudo apt-get -qq -y install libagg-dev
sudo apt-get -qq -y install apache2 apache2-threaded-dev apache2-mpm-prefork apache2-utils

#####################################################################
# Create directory structure
#####################################################################
echo "Preparing directory structure"
cd ~
mkdir src bin planet

#####################################################################
# Get the planet file for import, defined above.
# Remember to change this to point to the file you want to use, I put
# it on a local server in my network for faster transfer.
#####################################################################
echo "Getting planet file"
scp emil@192.168.10.101:/mnt/needle/planet/$OSM_IMPORT_FILE ~/planet/.
#wget http://planet.openstreetmap.org/$OSM_IMPORT_FILE ~/planet.

#####################################################################
# Get osm2pgsql from repository and build
#####################################################################
echo "Checkout and build osm2pgsql"
cd ~/bin
svn co http://svn.openstreetmap.org/applications/utils/export/osm2pgsql/
cd osm2pgsql
./autogen.sh
./configure
make

#####################################################################
# Make configuration changes to postgresql:
#   shared_buffers = 128MB # 16384 for 8.1 and earlier
#   checkpoint_segments = 20
#   maintenance_work_mem = 256MB # 256000 for 8.1 and earlier
#   autovacuum = off
#####################################################################
echo "Configuring PostGIS database"
sudo cp /etc/postgresql/8.4/main/postgresql.conf /etc/postgresql/8.4/main/postgresql.conf.bak
cat /etc/postgresql/8.4/main/postgresql.conf | sed 's/^.*shared_buffers.*$/shared_buffers = 128MB/' > postgresql.conf-new
cat postgresql.conf-new | sed 's/^.*checkpoint_segments.*$/checkpoint_segments = 20/' > postgresql.conf-new2
cat postgresql.conf-new2 | sed 's/^.*autovacuum =.*$/autovacuum = off/' > postgresql.conf-new
if ( grep 'maintenance_work_mem = 256MB' postgresql.conf-new ); then
    echo "maintenance_work_mem already defined!"
else
    echo "maintenance_work_mem = 256MB" >> postgresql.conf-new
fi

sudo cp postgresql.conf-new /etc/postgresql/8.4/main/postgresql.conf
rm postgresql.conf-new
rm postgresql.conf-new2

#####################################################################
# Edit kernel parameter shmmax to increase maximum size of shared memory.
#####################################################################
echo "Editing kernel parameter shmmax to increase maximum size of shared memory"
sudo sh -c "echo 'kernel.shmmax=268435456' > /etc/sysctl.d/60-shmmax.conf"
sudo service procps start

#####################################################################
# Restarting postgresql so that changes are in effect.
#####################################################################
echo "Restarting postgresql"
sudo /etc/init.d/postgresql restart

#####################################################################
# Add current user as postgres user and setup GIS tables
#####################################################################
echo "Setting up database user and tables"
echo "$USER" > /tmp/curr_user
sudo su - postgres -c 'CURR_USER=`cat /tmp/curr_user`; createuser -s $CURR_USER'
sudo su - postgres -c 'CURR_USER=`cat /tmp/curr_user`; createdb -E UTF8 -O $CURR_USER gis'
sudo su - postgres -c 'createlang plpgsql gis'
rm /tmp/curr_user

psql -f /usr/share/postgresql/8.4/contrib/postgis-1.5/postgis.sql -d gis
echo "ALTER TABLE geometry_columns OWNER TO $USER; ALTER TABLE spatial_ref_sys OWNER TO $USER;" | psql -d gis
psql -f /usr/share/postgresql/8.4/contrib/_int.sql -d gis
psql -f ~/bin/osm2pgsql/900913.sql -d gis

#####################################################################
# Get mapnik from repository and build
#####################################################################
echo "Checkout and build Mapnik"
cd ~/src
svn co http://svn.mapnik.org/tags/release-0.7.1/ mapnik
cd mapnik
python scons/scons.py configure INPUT_PLUGINS=all OPTIMIZATION=3 SYSTEM_FONTS=/usr/share/fonts/truetype/
python scons/scons.py
sudo python scons/scons.py install
sudo ldconfig

#####################################################################
# Get mapnik tools from repository
#####################################################################
echo "Installing Mapnik tools"
cd ~/bin
svn co http://svn.openstreetmap.org/applications/rendering/mapnik

#####################################################################
# Download and unpack prepared world boundary data. 
#####################################################################
echo "Download and unpack prepared world boundary data"
cd ~/bin/mapnik
mkdir world_boundaries

#wget http://tile.openstreetmap.org/world_boundaries-spherical.tgz
scp emil@192.168.10.101:/mnt/needle/world_boundary_data/world_boundaries-spherical.tgz .
tar xvzf world_boundaries-spherical.tgz

#wget http://tile.openstreetmap.org/processed_p.tar.bz2
scp emil@192.168.10.101:/mnt/needle/world_boundary_data/processed_p.tar.bz2 .
tar xvjf processed_p.tar.bz2 -C world_boundaries

#wget http://tile.openstreetmap.org/shoreline_300.tar.bz2
scp emil@192.168.10.101:/mnt/needle/world_boundary_data/shoreline_300.tar.bz2 .
tar xjf shoreline_300.tar.bz2 -C world_boundaries

#wget http://www.naturalearthdata.com/http//www.naturalearthdata.com/download/10m/cultural/10m-populated-places.zip
scp emil@192.168.10.101:/mnt/needle/world_boundary_data/10m-populated-places.zip .
unzip 10m-populated-places.zip -d world_boundaries

#wget http://www.naturalearthdata.com/http//www.naturalearthdata.com/download/110m/cultural/110m-admin-0-boundary-lines.zip
scp emil@192.168.10.101:/mnt/needle/world_boundary_data/110m-admin-0-boundary-lines.zip .
unzip 110m-admin-0-boundary-lines.zip -d world_boundaries

#####################################################################
# Generate the XML file containing DB settings.
#####################################################################
echo "Generate include file"
cd ~/bin/mapnik
./generate_xml.py --dbname gis --user $USER --accept-none

#####################################################################
# Get mod_tile from repository, build and install
#####################################################################
echo "Checkout and build mod_tile"
cd ~/src
svn co http://svn.openstreetmap.org/applications/utils/mod_tile
cd mod_tile
make
sudo make install

#####################################################################
# Install mapnik fonts
#####################################################################
echo "Installing mapnik fonts"
sudo mkdir -p /usr/local/lib/mapnik/fonts
sudo cp -R ~/src/mapnik/fonts/* /usr/local/lib/mapnik/fonts/

#####################################################################
# Create directory where renderd stores generated tiles
#####################################################################
echo "Creating tile directory"
sudo mkdir /var/lib/mod_tile
sudo chmod 777 /var/lib/mod_tile

#####################################################################
# Create directory where renderd creates sockets
#####################################################################
echo "Creating socket directory for renderd"
sudo mkdir /var/run/renderd
sudo chown $USER:$USER /var/run/renderd

#####################################################################
# Create renderd.conf in /etc/
#####################################################################
echo "Creating renderd.conf in /etc/"
cat << EOF | sudo tee /etc/renderd.conf
[renderd]
socketname=/var/run/renderd/renderd.sock
num_threads=4
tile_dir=/var/lib/mod_tile ; DOES NOT WORK YET                                                                                                            
stats_file=/var/run/renderd/renderd.stats

[mapnik]
plugins_dir=/usr/local/lib/mapnik/input
font_dir=/usr/local/lib/mapnik/fonts
font_dir_recurse=1

[default]
URI=/$TILE_CONTEXT/
XML=/home/$USER/bin/mapnik/osm.xml
HOST=$TILE_HOST
;HTCPHOST=proxy.openstreetmap.org 
EOF

#####################################################################
# Create apache site config as virtual host
#####################################################################
echo "Creating apache site for tiles"
cat << EOF | sudo tee /etc/apache2/sites-available/$TILE_CONTEXT
# This is the Apache server configuration file for providing OSM tile support
# through mod_tile

LoadModule tile_module /usr/lib/apache2/modules/mod_tile.so

<VirtualHost *:80>
    ServerName $TILE_HOST
    #ServerAlias a.tile.openstreetmap.org b.tile.openstreetmap.org c.tile.openstreetmap.org d.tile.openstreetmap.org
    DocumentRoot /var/www/$TILE_CONTEXT

    # You can either manually configure each tile set
    #AddTileConfig /folder/ TileSetName

    # or load all the tile sets defined in the configuration file into this virtual host
    LoadTileConfigFile /etc/renderd.conf

    # Timeout before giving up for a tile to be rendered
    ModTileRequestTimeout 3

    # Timeout before giving up for a tile to be rendered that is otherwise missing
    ModTileMissingRequestTimeout 10

    # If tile is out of date, don't re-render it if past this load threshold (users gets old tile)
    ModTileMaxLoadOld 2

    # If tile is missing, don't render it if past this load threshold (user gets 404 error)
    ModTileMaxLoadMissing 5

    # Socket where we connect to the rendering daemon
    ModTileRenderdSocketName /var/run/renderd/renderd.sock

    ##
    ## Options controlling the cache proxy expiry headers. All values are in seconds.
    ##
    ## Caching is both important to reduce the load and bandwidth of the server, as
    ## well as reduce the load time for the user. The site loads fastest if tiles can be
    ## taken from the users browser cache and no round trip through the internet is needed.
    ## With minutely or hourly updates, however there is a trade-off between cacheability
    ## and freshness. As one can't predict the future, these are only heuristics, that
    ## need tuning.
    ## If there is a known update schedule such as only using weekly planet dumps to update the db,
    ## this can also be taken into account through the constant PLANET_INTERVAL in render_config.h
    ## but requires a recompile of mod_tile

    ## The values in this sample configuration are not the same as the defaults
    ## that apply if the config settings are left out. The defaults are more conservative
    ## and disable most of the heuristics.

    ## Caching is always a trade-off between being up to date and reducing server load or
    ## client side latency and bandwidth requirements. Under some conditions, like poor
    ## network conditions it might be more important to have good caching rather than the latest tiles.
    ## Therefor the following config options allow to set a special hostheader for which the caching
    ## behaviour is different to the normal heuristics
    ##
    ## The CacheExtended parameters overwrite all other caching parameters (including CacheDurationMax)
    ## for tiles being requested via the hostname CacheExtendedHostname
    #ModTileCacheExtendedHostname cache.tile.openstreetmap.org
    #ModTileCacheExtendedDuration 2592000

    # Upper bound on the length a tile will be set cacheable, which takes
    # precedence over other settings of cacheing
    ModTileCacheDurationMax 604800

    # Sets the time tiles can be cached for that are known to by outdated and have been
    # sent to renderd to be rerendered. This should be set to a value corresponding
    # roughly to how long it will take renderd to get through its queue. There is an additional
    # fuzz factor on top of this to not have all tiles expire at the same time
    ModTileCacheDurationDirty 900

    # Specify the minimum time mod_tile will set the cache expiry to for fresh tiles. There
    # is an additional fuzz factor of between 0 and 3 hours on top of this.
    ModTileCacheDurationMinimum 10800

    # Lower zoom levels are less likely to change noticeable, so these could be cached for longer
    # without users noticing much.
    # The heuristic offers three levels of zoom, Low, Medium and High, for which different minimum
    # cacheing times can be specified.

    #Specify the zoom level below  which Medium starts and the time in seconds for which they can be cached
    ModTileCacheDurationMediumZoom 13 86400

    #Specify the zoom level below which Low starts and the time in seconds for which they can be cached
    ModTileCacheDurationLowZoom 9 518400

    # A further heuristic to determine cacheing times is when was the last time a tile has changed.
    # If it hasn't changed for a while, it is less likely to change in the immediate future, so the
    # tiles can be cached for longer.
    # For example, if the factor is 0.20 and the tile hasn't changed in the last 5 days, it can be cached
    # for up to one day without having to re-validate.
    ModTileCacheLastModifiedFactor 0.20

    ## Tile Throttling
    ## Tile scrappers can often download large numbers of tiles and overly staining tileserver resources
    ## mod_tile therefore offers the ability to automatically throttle requests from ip addresses that have
    ## requested a lot of tiles.
    ## The mechanism uses a token bucket approach to shape traffic. I.e. there is an initial pool of n tiles
    ## per ip that can be requested arbitrarily fast. After that this pool gets filled up at a constant rate
    ## The algorithm has to metrics. One based on overall tiles served to an ip address and a second one based on
    ## the number of requests to renderd / tirex to render a new tile.

    ## Overall enable or disable tile throttling
    ModTileEnableTileThrottling Off
    
    ## Parameters (poolsize in tiles and topup rate in tiles per second) for throttling tile serving.
    ModTileThrottlingTiles 10000 1

    ## Parameters (poolsize in tiles and topup rate in tiles per second) for throttling render requests.
    ModTileThrottlingRenders 128 0.2

    ## increase the log level for more detailed information
    LogLevel debug

</VirtualHost>
EOF

#####################################################################
# Enable the new virtual host in Apache
#####################################################################
sudo a2ensite $TILE_CONTEXT

#####################################################################
# Create service definition for renderd
#####################################################################
echo "Writing service definition for renderd"
cat << EOF | sudo tee /etc/init/renderd.conf
description "mod_tile renderd service"
author      "Emil Breding"

start on local-filesystems
stop on runlevel [!2345]

respawn

pre-start script
    mkdir -p /var/run/renderd
    chown $USER:$USER /var/run/renderd
end script

script
    exec sudo su - $USER -c '/home/$USER/src/mod_tile/renderd -f'
end script
EOF

#####################################################################
# Add demo site with openlayers and slippy map
#####################################################################
echo "Setting up slippy map"
sudo mkdir -p /var/www/$TILE_CONTEXT/api/img
cd /var/www/$TILE_CONTEXT/api
sudo wget http://www.openlayers.org/api/OpenLayers.js
cd /var/www/$TILE_CONTEXT/api/img
sudo wget http://www.openlayers.org/api/img/blank.gif
sudo wget http://www.openlayers.org/api/img/north-mini.png
sudo wget http://www.openlayers.org/api/img/zoom-minus-mini.png
sudo wget http://www.openlayers.org/api/img/west-mini.png
sudo wget http://www.openlayers.org/api/img/east-mini.png
sudo wget http://www.openlayers.org/api/img/south-mini.png
sudo wget http://www.openlayers.org/api/img/zoom-plus-mini.png
sudo wget http://www.openlayers.org/api/img/zoom-world-mini.png
sudo mkdir -p /var/www/$TILE_CONTEXT/api/theme/default
cd /var/www/$TILE_CONTEXT/api/theme/default
sudo wget http://www.openlayers.org/api/theme/default/style.css

cat << EOF | sudo tee /var/www/$TILE_CONTEXT/index.html
<html>
  <head>
    <title>OpenLayers Demo</title>
    <style type="text/css">
      html, body, #basicMap {
          width: 100%;
          height: 95%;
          margin: 0;
      }
    </style>
    <script src="api/OpenLayers.js"></script>
    <script>
      function init() {
        map = new OpenLayers.Map("basicMap");
        var newLayer = new OpenLayers.Layer.OSM("New Layer", "/$TILE_CONTEXT/\${z}/\${x}/\${y}.png", {numZoomLevels: 19});
        map.addLayer(newLayer);
        map.setCenter(new OpenLayers.LonLat(18.060,59.337) // Center of the map
          .transform(
            new OpenLayers.Projection("EPSG:4326"), // transform from WGS 1984
            new OpenLayers.Projection("EPSG:900913") // to Spherical Mercator Projection
          ), 15 // Zoom level
        );
      }
    </script>
  </head>
  <body onload="init();">
    <div id="basicMap"></div>
  </body>
</html>
EOF

#####################################################################
# Restart Apache and renderd
#####################################################################
sudo service renderd start
sudo /etc/init.d/apache2 restart
printf 'Elapsed time for OSM setup: %s\n' $(timer $t_setup)

t_import=$(timer)

#####################################################################
# Import the choosen planet file
#####################################################################
echo "Importing OSM to database, this will take a while"
cd ~/bin/osm2pgsql
./osm2pgsql -S default.style --slim -d gis -C 2048 ~/planet/$OSM_IMPORT_FILE

echo "Generate map of Englad"
cd ~/bin/mapnik
./generate_image.py

printf 'Elapsed time for osm2pgsql import: %s\n' $(timer $t_import)

#####################################################################
# All done!
#####################################################################
echo ""
echo "OSM setup and GIS import is done!"
echo ""
echo "You may now open http://$TILE_HOST in your browser to make sure that tile generation is working"
echo "Status for the renderd daemon may be checked here: http://$TILE_HOST/mod_tile"
echo "renderd logs to: /var/log/syslog"
echo ""
