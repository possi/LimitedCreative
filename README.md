# Limited Creative
http://dev.bukkit.org/server-mods/limited-creative/

Required dependencies
---------------------

* [Bukkit](https://github.com/Bukkit/Bukkit)

Maven Dependencies for optional integrations
--------------------------------------------

* [WorldGuard](https://github.com/sk89q/worldguard)
* [WorldEdit](https://github.com/sk89q/worldedit)

Non-Maven dependencies for optional integrations
------------------------------------------------

* [xAuth](http://dev.bukkit.org/server-mods/xauth/)
* [AuthMe](http://dev.bukkit.org/server-mods/authme-reloaded/)
* [Multiverse-Core](http://dev.bukkit.org/server-mods/multiverse-core/)

----

To use Maven packaging
----------------------

You need to add the non-maven dependencies:

* download the jars and then...
* $ mvn install:install-file -Dfile=AuthMe.jar -DgroupId=uk.org.whoami -DartifactId=authme -Dversion=2.6.2 -Dpackaging=jar
* $ mvn install:install-file -Dfile=Multiverse-Core.jar -DgroupId=com.onarandombox -DartifactId=multiverse-core  -Dversion=2.3-AB -Dpackaging=jar
* $ mvn install:install-file -Dfile=xAuth.jar -DgroupId=com.cypherx -DartifactId=xauth -Dversion=3.1 -Dpackaging=jar