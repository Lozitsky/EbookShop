# create root
#mysqld.exe --initialize-insecure --user=mysql
mysql -u root
create user if not exists 'admin_db' identified by 'password';
create database if not exists ebookshop;
show databases;
grant all on ebookshop.* to 'admin_db';
show grants;
show grants for admin_db;
exit
	# reboot service MySQL
	net stop mysql
	net start mysql
	# login as admin db
	mysql -u admin_db -p password
show databases;
use ebookshop
show tables;