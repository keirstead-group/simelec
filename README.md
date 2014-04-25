# SimElec

SimElec is a model for simulating domestic electricity consumption at
one-minute intervals.  

It was originally developed at Loughborough University by Ian
Richardson and Murray Thomson and it was first written in Excel using
VBA.  A Perl version has also been created by
[Gergely Acs](http://www.crysys.hu/~acs/misc.html).  More details can
be found at the
[SimElec homepage](http://homepages.lboro.ac.uk/~eliwr/#software).

## Use

To run the software, launch the `SimElecUI.java` application.  The
user interface will ask you to specify an output directory, as well as
choosing options for what sort of household and day to simulate, and
which model or models to run.  SimElec currently supports simulation
of appliance and lighting loads.  

The results will be placed in the specified output directory in the
form of three `csv` files containing the household occupancy, lighting
loads, and appliances loads (assuming all models were selected).
These can be analysed using the scripts in the `R` directory.

## Further information

This version of SimElec is maintained by James Keirstead
(j.keirstead@imperial.ac.uk) at the Department of Civil and
Environmental Engineering, Imperial College London.

Version 0.1.1
Released 25-04-2014
