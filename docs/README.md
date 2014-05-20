# SimElec

SimElec is a Java model for simulating domestic electricity
consumption at one-minute intervals.

It was originally developed at Loughborough University by Ian
Richardson and Murray Thomson and was written in Excel using VBA.  A
Perl version has also been created by
[Gergely Acs](http://www.crysys.hu/~acs/misc.html).  

This version is maintained by James Keirstead at the Department of
Civil and Environmental Engineering, Imperial College London.

## System requirements

To use SimElec, you must have a Java runtime environment installed,
version 7 update 6 or later.

To generate the R plots, you must have [R](http://www.r-project.org)
installed with the `Rscript` command available from the command line.
The `ggplot2`, `scales`, and `plyr` packages must be installed as
well.

## Use

SimElec can be run as either a standalone Java application with a user
interface, or as a code library without the interface.

### User interface

To use the GUI version, unzip the zip file on your local hard drive
and double-click `SimElec-${project.version}-jfx.jar` to launch the
application.

The user interface allows you to:

 * select the number of residents in the household, the month of the
   year to simulate, and whether to simulate a weekday or weekend.
 
 * specify an output directory.  The model results (`.csv` files for
   each model and if requested a summary plot) will be placed here.
   
 * select which models to run.  SimElec currently supports simulation
   of appliance and lighting loads.  For each model, you can also
   choose whether to report only the total demands or the
   disaggregated load profiles by appliance and bulb type.
   
 * calculate a grand total summing the demands from the appliance and
   lighting models.  These are placed in a `totals.csv` output file.
   
 * make a summary plot of the results using R.  This will show the
   demand profile over the course of the day with background shading
   to represent household occupancy levels.

### Code library

The two main points of entry into the code are `SimElec.java` which is
the main application and `SimElecUI.java` which launches the user
interface.  A simulation can therefore be run without the user
interface using code similar to:

```java 
// Build the model
int month = 7;
int nResidents = 5;
boolean weekend = false;
String outdir = "output";
SimElec simelec = new SimElec(month, nResidents, weekend, outdir)

// Set some options
simelec.setCalculateGrandTotals(true);
simelec.setMakeRPlots(false);

// Run the model
simelec.run();
```

## Further information

More details can be found at the
[SimElec homepage](http://homepages.lboro.ac.uk/~eliwr/#software).
The team at Loughborough University have also published a number of
academic articles describing the model:

 * Richardson, I., Thomson, M., & Infield, D. (2008). A
   high-resolution domestic building occupancy model for energy demand
   simulations. _Energy and Buildings_, 40(8),
   1560-1566. doi:[10.1016/j.enbuild.2008.02.006](http://dx.doi.org/10.1016/j.enbuild.2008.02.006)
   
 * Richardson, I., Thomson, M., Infield, D., & Delahunty,
   A. (2009). Domestic lighting: A high-resolution energy demand
   model. Energy and Buildings, 41(7),
   781-789. doi:[10.1016/j.enbuild.2009.02.010](http://dx.doi.org/10.1016/j.enbuild.2009.02.010)
   
 * Richardson, I., Thomson, M., Infield, D., & Clifford,
   C. (2010). Domestic electricity use: A high-resolution energy
   demand model. _Energy and Buildings_, 42(10),
   1878-1887. doi:[10.1016/j.enbuild.2010.05.023](http://dx.doi.org/10.1016/j.enbuild.2010.05.023)

The code is licensed under [GPL3](http://www.gnu.org/licenses/gpl-3.0.html).

Version ${project.version}
Released ${timestamp}

