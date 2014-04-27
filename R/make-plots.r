##' Make summary plots of all SimElec output
##'
##' James Keirstead
##' 11 March 2014
##'

root_dir <- "sample"

source("summary-plot.r")
make_summary_plot(root_dir)

source("occupancy.r")
make_occupancy_plot(root_dir)

source("lighting.r")
make_lighting_plot(root_dir)

source("appliances.r")
make_appliances_plot(root_dir)

source("irradiance.r")
