TwitterReader
==================

You can compile the application after installing sbt (http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html).
Start the

  sbt compile

command in the application root directory using the command line. Configuration files can be found in the conf/ directory. The application.prod.conf is the production configuration file. You can configure the terms to look for and the database connection parameters.

You can then run the application with the following command:

  target/universal/stage/bin/socialexplorer com.vodprofessionals.socialexplorer.Application


