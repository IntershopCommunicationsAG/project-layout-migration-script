# standard project layer support for ish cartridges

Since version 2.11, the ish gradle tools support the standard project layout for java projects
as described in the [Gradle Documentation](https://docs.gradle.org/current/userguide/java_plugin.html).
In order to support several other standard gradle plugins, it is recommended to use this project
layout for ish cartridges too.

These project provides a migration script, that supports the migration of existing cartridges.

## migration to a standard project layer

### moving source files project files into related standard folders:

#### "non-test" cartridges (!name.endsWith("_test"))

|target folder       | files                                                               |
|--------------------|---------------------------------------------------------------------|
| src/main/java      | `javasource/**/*.java { exclude 'tests/**/*' }`                     |
| src/test/java      | `javasource/tests/**/*.java`                                        |
| src/main/resources | `javasource/**/* { exclude '**/*.java', 'resources/tests/**/*' }`   |
| src/test/resources | `javasource/resources/tests/**/*`                                   |


#### "test" cartridges (name.endsWith("_test"))

|target folder       | files                                                               |
|--------------------|---------------------------------------------------------------------|
| src/main/java      | `javasource/**/*.java { exclude 'tests/com/**/*' }`                 |
| src/test/java      | `javasource/tests/com/**/*.java`                                    |
| src/main/resources | `javasource/**/* { exclude '**/*.java'}`                            |
| src/test/resources |                                                                     |

#### notes:

* The distribution of the resources is just an heuristic. Manual adaptions may be required.

* The leading 'resources' folder for all resources is required in order to keep references within java source files to
resources valid.


### further file adaptions

|                      | file pattern         | token                  | replaced by                |
|----------------------|---------------------|-------------------------|----------------------------|
| adapt genmodel's     | model/**/*.genmodel | `/javasource/`          | `src/main/java`            |
| javacc configuration | build.gradle        | `/javasource(.*\.jjt?)/`  | `src/main/resources/$path` |


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




