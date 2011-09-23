
From your shell, while in this (physion/trunk) directory, run the
command:

./bandt

The above script checks out the latest revision of the source code,
sets the CLASSPATH, compiles the .java files, and then runs the
class TestEBuilder.

If you have never run "svn co" from the command line, you might get a
complaint about security certificates.  If that happens, you will need
to get the certificate set by running svn from the command line.  You
will only need to do this once.  For example, do this:

    svn info https://svn.physionconsulting.com/ovpredicateui/trunk

The directory "src" and its subdirectories contain all the source code needed.
The directory "gui" contains test files that have nothing to do with running
the TestEBuilder program.
