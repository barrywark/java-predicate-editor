
There are two scripts you can use to checkout, build and run the
GUI: "bandt" and "cbandt".

"bandt" must be run from the physion/trunk directory.  It compiles
whatever code you currently have checked out, and runs the program.
For example, from your shell, while in this physion/trunk directory,
run the command:

./bandt

to build and run the program.  (This assumes you already have
the code checked out.)

"cbandt" checks out the latest, (or a specified revision),
of the code, builds it and runs it.

From your shell, run the command something like this:

./cbandt ~/test/test001

The above script checks out the latest revision of the source code,
sets the CLASSPATH, compiles the .java files, and then runs the
class TestEBuilder.

If you specify a revision number, e.g. 123 in the example below,
the script will check out that revision as opposed to the latest
revision.  For example:

./cbandt ~/test/test001 123

This is useful if you have a version that you know work and want
to build that one while the head of the tree is still being worked on.

If you have never run "svn co" from the command line, you might get a
complaint about security certificates.  If that happens, you will need
to get the certificate set by running svn from the command line.  You
will only need to do this once.  For example, do this:

    svn info https://svn.physionconsulting.com/ovpredicateui/trunk

The directory "src" and its subdirectories contain all the source code needed.
The directory "lib" and its subdirectories contain libraries, i.e. .jar files,
that are needed.
