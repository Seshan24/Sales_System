#!/bin/bash
# ---------------------------------------------------------------
# Sales Management System - Linux/macOS setup (creates DB, builds, deploys to dist/)
# Requirements: MySQL client + server, JDK 11+ and Maven
# ---------------------------------------------------------------
cd "$(dirname "$0")" || exit 1                                     # work from the project folder
read -p "MySQL user (e.g. root): " DBUSER                          # ask for the MySQL user
read -s -p "MySQL password: " DBPASS; echo                         # ask for the password (hidden)
echo "Creating database..."                                        # progress message
mysql -u"$DBUSER" -p"$DBPASS" < database/sales_db.sql || { echo "Database creation failed"; exit 1; }  # create schema + data
echo "Building application..."                                     # progress message
mvn -q clean package || { echo "Maven build failed"; exit 1; }     # compile + copy libraries
rm -rf dist && mkdir dist                                          # fresh deployment folder
cp target/sales-system-1.0.jar dist/ && cp -r target/lib dist/lib  # copy jar and libraries
cat > dist/db.properties <<CFG
db.url=jdbc:mysql://localhost:3306/sales_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=$DBUSER
db.password=$DBPASS
CFG
printf '#!/bin/bash\ncd "$(dirname "$0")"\njava -jar sales-system-1.0.jar\n' > dist/run.sh && chmod +x dist/run.sh  # launcher
echo "SETUP COMPLETE. Start with dist/run.sh   (admin/admin123 or cashier1/cashier123)"  # finish message
