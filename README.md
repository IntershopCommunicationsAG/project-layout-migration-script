# standard project layer support for ish cartridges

Since version 2.11, the ish gradle tools support the standard project layout for java projects
as described in the [Gradle Documentation](https://docs.gradle.org/current/userguide/java_plugin.html).
In order to support several other standard gradle plugins, it is recommended to use this project
layout for ish cartridges too.

These project provides a migration script, that supports the migration of existing cartridges.
The following steps will be done:

* files from the javasource folder will be moved into into the recommended layout
  with using the `svn move` command:

|target folder          | files                                                   |
|-----------------------|---------------------------------------------------------|
| src/main/java         | `javasource/**/*.java { exclude 'tests/com/**/*' }`     |
| src/main/resources    | `javasource/resources/**/*`                             |
| src/test/java         | `javasource/tests/com/**/*.java`                        |

* package names within unit tests will be corrected.


## preconditions

`com.intershop.build.gradle:ish-component-plugin:2.11`


## usage

For migrating all cartridges of an existing component set:

* within your component set project: `gradlew -I <path_to_migration_project>/migrate.gradle migrate`

The migration script does not process a commit. So after the execution of the script, some additional checks can be
and and are recommended:

* call `gradlew check publish`
* it is recommended to search for 'javasource' within all migrated in order adapt it, in order to identify
  additional required migration steps.




