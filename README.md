# Valar infrastructure / backend

This repository contains code supporting [Valar](https://github.com/dmyersturnbull/valar-schema).
Relational database. [The Valar](https://en.wikipedia.org/wiki/Vala_(Middle-earth)) are gods who entered the World.

**NOTE:** This code is a port that is currently missing components. It will not function correctly yet.

**ALSO NOTE:** This refers to "goldberry", which is misnamed.



## Table of Contents 
* [Database organization](#db_info)
  * [Organization](#db_organization)
  * [Interacting](#interacting)
  * [Legacy data](#legacy_data)
* [Core Scala code](#source_code)
  * [Organization](#code_organization)
  * [Code setup](#code_setup)
  * [Generating Slick tables](#slick)
* [Job queue with Airflow](#setup_airflow)
  * [Creating the Conda Environment ](#conda_env)
  * [Airflow setup](#airflow_db)
  * [Airflow DB configuration](#db_config)
  * [dags_to_create table](#dags_to_create)
  * [Airflow configs](#goldberry)


<a name="db_info"/>
## Database information

<a name="db_organization"/>
### database organization


The database itself is split into a few pieces. To learn more about the structure, see the ERDs.
- the core behavioral tables
- the chemical annotation tables, names beginning with _mandos\__
- the biomarker (mass spec and expression) tables, names beginning with _biomarker\__


<a name="interacting"/>
### interacting with the database

After logging into Valinor, run `mysql -u username -p` and enter the database password. Then run `use valar;` to enter the database.
Some queries of interest might be:

```sql
show tables;
describe experiments;
show create table experiments;  # will show foreign keys (links between tables)
select * from experiments;
select * from batteries limit 5;
select id from assays where name like '%vsr1%';
```

Valar enforces a maximum number of bytes returned for any query of 640MB. This was chosen to be _just_ enough to load all of Capria’s reference set of 126 plates, the largest experiment by number of bytes we have. However, I recommend avoiding querying that much data all at once and instead querying it in chunks.

<a name="legacy_data"/>
## legacy data

You can determine which runs were run with SauronX: They have `runs.submission` set. Data prior to this have some quirks.

- `plates.datetime_plated` will be null.
- `plate_runs.datetime_dosed` might be null.
- `plate_runs.datetime_run` might be wrong. Although unlikely, it could be off by a day.
- Different sensor and timing information will be available.
- Raw data is stored in a different format and under a different path.
- SauronX assays are defined for a fixed length of time, and their stimulus frames are defined in milliseconds. In pre-SauronX assays bytes in `stimulus_frames.frames` correspond to frames sampled at a particular framerate. The sampling might for a plate run might be defined in `sensor_data` for sensor `legacy-assay-milliseconds`.



<a name="source_code"/>
## Source code


<a name="code_organization"/>
### code organization

  - `core` contains the table schema and some utilities
  - `params` provides convenience functions for assay and plate template parameterizations as used in Valinor
  - `insertion` inserts and updates projects, plates, compounds, assays, etc.
  - `importer` processes data from [SauronX](https://github.com/kokellab/sauronx)


<a name="code_setup"/>
### Scala setup

You’ll only want to build and use this project if you’re writing in Scala. For Python connections to the database, see [valarpy](https://github.com/kokellab/valarpy).

First, you will need to set the environment variables `valar_user` and `valar_password`. Some of these are listed on Valinor under `/etc/admin/security/db_users.list`. You’ll need to be the root user to see anything under `/etc/admin/security`.

First, you will need `conf/application.conf`. An example is shown here:

```
driver = "slick.jdbc.MySQLProfile"
valar_db {
	url = "jdbc:mysql://127.0.0.1:3306/valar?user=dbuser&password=dbpassword&useJDBCCompliantTimezoneShift=true&serverTimezone=America/Los_Angeles&nullNamePatternMatchesAll=true"
	driver = com.mysql.cj.jdbc.Driver
	maxThreads = 4
	maxConnections = 4
	maximumPoolSize = 10
}
valar_api_key=...
chemspiderToken=...
valar_api_key=...
```

0. Install Scala 2.12, Maven, and Simple Build Tool (sbt) 1.0+: `brew install scala maven sbt` or preferably manual download on Linux
1. Clone [skale](https://github.com/kokellab/skale) (`git clone https://github.com/dmyersturnbull/skale`)
2. Build skale and publish it to your local Ivy repository: `cd skale; sbt publishLocal`
3. Navigate back to valar and build only the core subproject with run `sbt core/publishLocal`
4. Clone [lorien](https://github.com/kokellab/lorien) and publish it with sbt (`sbt publishLocal`)
5. Navigate back to valar and publish the full project with `sbt compile`
6. To run the tests, run `sbt test`
7. To use Valar in another project, run `sbt publishLocal`


<a name="slick"/>
### [Slick](https://github.com/slick/slick) table generation

`core/src/main/scala/kokellab/valar/core/Tables.scala` is generated from the database schema using Slick.
To regenerate it, run `sbt slick-gen-tables`. Note that it will use the connection information supplied in `config/app.properties`.

Additionally, generate the schema using `scripts/dump-schema.sh <username>`.


<a name="setup_airflow"/>
## Setting up Airflow



Goldberry relies on [Airflow](https://airflow.apache.org/) to schedule its tasks. Follow these instructions to set up Airflow for use on a machine (e.g: Valinor/Celebrant). 

<a name="conda_env"/>

### Creating the Conda Environment 
1. Create a conda environment named airflow. 
```conda create --name airflow python=3.7```
2. Run `sudo apt-get install python-dev` ,`sudo apt-get install gcc`,`sudo apt-get install libmysqlclient-dev`, `sudo apt-get install p7zip-full`, `sudo apt-get install ffmpeg` so that you have the necessary dependencies to install `airflow`. 
3. Switch to the airflow environment and then install airflow and slackclient with pip.
    ```
    conda activate airflow 
    pip install apache-airflow 
    pip install slackclient==1.3.1
    pip install mysqlclient

    ```
4. Create/export an environment variable that specifies where the airflow home (e.g: /var/airflow) will be.
    ```
    export AIRFLOW_HOME = /path_where_you_want_airflow_home_to_be
    ```
    With the above command, the environment variable isn't permanent so I recommend adding this to the `.bash_profile` so      that you don't have to repeat the command for every terminal instance. 

<a name="airflow_db"/>

### Setting up the Airflow Database

1. Create the `airflow` database that will be used. 
    ```
    mysql -u root
    mysql> CREATE DATABASE airflow CHARACTER SET utf8 COLLATE utf8_unicode_ci;
    mysql> create user 'airflow'@'localhost' identified by 'INSERT_PASSWORD_HERE';
    mysql> grant all privileges on airflow.* to 'airflow'@'localhost';
    mysql> flush privileges;
    ```
    This creates a database user `airflow` with the password `INSERT_PASSWORD_HERE`. 
2. Activate the `airflow` environment and then run the `initdb` command. This will instantiate some airflow files (e.g: `airflow.cfg`) and set up the default Airflow DB which uses `sqlite`. We can't use `sqlite` as it prevents the processing of multiple dags (e.g: submissions) at the same time. 
    ```
    conda activate airflow
    airflow initdb
    ```
3. Open up the `$AIRFLOW_HOME/airflow.cfg` file on a text editor and change the executor from `CeleryExecutor` to `LocalExecutor`. Also, set the `sql_alchemy_conn` to have the correct database credentials. 
   ```
   executor = LocalExecutor
   sql_alchemy_conn = mysql://airflow:INSERT_PASSWORD_CHOSEN_IN_STEP_FIVE@localhost:3306/airflow
   ```
4. Initialize the Airflow DB again to reflect the changes you made. This will make `mysql/mariadb` the primary db and allow you to have multiple dags running at the same time. 
    ```
    airflow initdb
    ```
    
<a name="db_config"/>

### Setting up the correct DB configurations
After the Airflow DB is initialized, you will most likely be prompted to set the global variable explicit_defaults_for_timestamp. Make the following changes to the `my.cnf` file. 
1. Open up the MariaDB configuration file in a text editor of your choice. 
    ```
    vim /etc/mysql/my.cnf
    ```
2. Under the section `[mysqld]`, add the following settings: 
    ```
    wait_timeout = 604800
    interactive_timeout = 604800 
    max_allowed_packet=2GB
    explicit_defaults_for_timestamp = 1
    default-time-zone='-7:00'
    ```
    If the section doesn't exist, add it to the file and put the settings below it. 
3. Under the section `[mysqldump]`, add the following settings: 
    ```
    [mysqldump]
    max_allowed_packet=2GB
    ```
4. Restart MariaDB.
    ```
    systemctl restart mariadb.service
    ```
    
<a name="dags_to_create"/>    
    
### Add the Dags_to_create table
1. Refer to the table_create.sql file under goldberry/scripts/valar. Create the dags_to_create table in the valar database. Import it or just run the command from mysql. 
    ```
    mysql -u root
    mysql>CREATE TABLE `dags_to_create` (
  	`id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  	`submission_hash` char(12) NOT NULL,
  	`dag_created` tinyint(1) NOT NULL default 0,
  	`created` timestamp NOT NULL DEFAULT current_timestamp(),
  	PRIMARY KEY (`id`),
  	UNIQUE KEY `id_hash_hex` (`submission_hash`)
	)
    ```

<a name="goldberry"/>    

### Set Airflow configs for use with Goldberry
1. (OPTIONAL, but recommended) Get rid of airflow example dags with https://stackoverflow.com/questions/43410836/how-to-remove-default-example-dags-in-airflow. 
2. Run the webserver command on Celebrant/Valinor. 
    ```
    airflow webserver -p 8080  #ANY PORT is fine
    ```
3. Set up an ssh tunnel from your machine to valinor.
    ```
    ssh -L 7777:localhost:8080 valinor
    ```
4. Navigate to the following URL `http://localhost:7777/admin/connection/` and you will see the airflow UI. 
5. Go to Admin > Connections > Create and Create a connection called `airflow_db` with the settings shown in the picture below. 
![Screen Shot 2019-09-26 at 6 18 23 PM](https://user-images.githubusercontent.com/10649054/65734962-32914280-e08a-11e9-9c6d-d3242c2a5252.png)
6. Create another connection called `dag_db`. 
![Screen Shot 2019-09-26 at 6 22 00 PM](https://user-images.githubusercontent.com/10649054/65735064-8bf97180-e08a-11e9-9dfc-45b108d11e08.png)
Fill out the `login` field with the mysql user and `password` field with the password for the user. 
7. Create the last connection called `slack`. 
![Screen Shot 2019-09-26 at 6 23 28 PM](https://user-images.githubusercontent.com/10649054/65735099-c6630e80-e08a-11e9-8595-b5d8dab5ce42.png)
Fill out the `password` field with the slack token.
8. Go to Admin > Pools > Create and add the following Pools:
Add the following pools under admin/pools:
	- insertion_pool (6 slots)
	- archive_pool (3 slots)

Congrats! Goldberry-airflow should now be ready-to-go!
