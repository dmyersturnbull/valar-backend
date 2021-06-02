# Valar infrastructure / backend

This repository contains code supporting [Valar](https://github.com/dmyersturnbull/valar-schema).
Relational database. [The Valar](https://en.wikipedia.org/wiki/Vala_(Middle-earth)) are gods who entered the World.

**NOTE:** This code is a port that is currently missing components. It will not function correctly yet.


## Table of Contents 
* [Database organization](#db_info)
  * [Organization](#db_organization)
  * [Interacting](#interacting)
  * [Legacy data](#legacy_data)
* [Core Scala code](#source_code)
  * [Organization](#code_organization)
  * [Code setup](#code_setup)
  * [Generating Slick tables](#slick)


<a name="db_info"/>
## Database information

<a name="db_organization"/>
### database organization


The database itself is split into a few pieces. To learn more about the structure, see the ERDs.
- the core behavioral tables
- the chemical annotation tables, names beginning with _mandos\__


<a name="interacting"/>
### interacting with the database

After logging into Valinor, run `mysql -u username -p` and enter the database password.
Then run `use valar;` to enter the database.
Some queries of interest might be:

```sql
show tables;
describe experiments;
show create table experiments;  # will show foreign keys (links between tables)
select * from experiments;
select * from batteries limit 5;
select id from assays where name like '%vsr1%';
```

Valar enforces a maximum number of bytes returned for any query of 640MB.
This was chosen to be _just_ enough to load all of Capria’s reference set of 126 plates,
the largest experiment by number of bytes we have.
However, I recommend avoiding querying that much data all at once and instead querying it in chunks.

<a name="legacy_data"/>
## legacy data

You can determine which runs were run with SauronX: They have `runs.submission` set.
Data prior to this have some quirks.

- `plates.datetime_plated` will be null.
- `runs.datetime_dosed` might be null.
- `runs.datetime_run` might be wrong. Although unlikely, it could be off by a day.
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
  - `importer` processes data from [SauronX](https://github.com/dmyersturnbull/sauronx)


<a name="code_setup"/>
### Scala setup

You’ll only want to build and use this project if you’re writing in Scala.
For Python connections to the database, see [valarpy](https://github.com/dmyersturnbull/valarpy).

First, you will need to set the environment variables `valar_user` and `valar_password`.
Some of these are listed on Valinor under `/etc/admin/security/db_users.list`. You’ll need to be the root user to see anything under `/etc/admin/security`.

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
```

0. Install Scala 3.0, Maven, and Simple Build Tool (sbt) 1.5+.
1. Clone [pippin](https://github.com/dmyersturnbull/pippin) (`git clone https://github.com/dmyersturnbull/pippin`)
2. Build pippin and publish it to your local Ivy repository: `cd pippin; sbt publishLocal`
3. Navigate back to valar and build only the core subproject with run `sbt core/publishLocal`
4. Navigate back to valar and publish the full project with `sbt compile`
5. To run the tests, run `sbt test`
6. To use Valar in another project, run `sbt publishLocal`


<a name="slick"/>
### [Slick](https://github.com/slick/slick) table generation

`core/src/main/scala/dmyersturnbull/valar/core/Tables.scala` is generated from the database schema using Slick.
To regenerate it, run `sbt slick-gen-tables`. Note that it will use the connection information supplied in `config/app.properties`.

Additionally, generate the schema using `scripts/dump-schema.sh <username>`.

