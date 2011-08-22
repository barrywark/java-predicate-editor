This is the start of the "Expression Builder" GUI.  All these files
will be moved around and changed long before the GUI is completed.

The contects of this directory are simply a way to get something up
and running using the Physion Subversion source code control.


In this directory:

bandt -
    Stands for "build and test".  This script will build and run
    a simple test program.  It is simply a way for a developer to
    check whether they have broken the build.
    Currently, this script just calls javac and java, but will eventually
    call ant.  In addition, this script will do a checkout and then
    build and test.

TestEBuilder.java -
    Starts the GUI.

