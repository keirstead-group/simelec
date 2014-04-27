##' A script to build the summary plot
##'
##' This can be called from the command line as
##' 
##' > Rscript make-summary-plot.r directory
##'
##' where `directory` is the location of the SimElec output files
##'
args <- commandArgs(TRUE)
source("summary-plot.r")
make_summary_plot(args[1], args[2])
