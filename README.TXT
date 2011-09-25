
There are two scripts you can use: "bandt" and "cbandt".

"bandt" must be run from the physion/trunk directory.  It compiles
and runs the program.  For example, from your shell, while in
this physion/trunk directory, run the command:

./bandt

to build and run the program.  (This assumes you already have
the code checked out.)

"cbandt" can be run from any directory, (but you must specify a
directory), and it checks out the latest, (or a specified revision),
of the code, builds it and runs it.

From your shell, run the command something like this:

./cbandt ~/test/test001

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
