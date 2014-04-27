##' Makes a single summary plot of SimElec output
##'
##' @author James Keirstead
##' @param dir a character vector specifying the directory containing
##' @param file the output file name
##' the SimElec output files
make_summary_plot <- function(dir, file) {

    ## Load in the required code
    source("appliances.r")
    source("lighting.r")
    source("occupancy.r")
    
    ## Get the data
    power <- get_appliance_data(dir)
    lights <- get_lighting_data(dir)
    lights <- cbind(id="LIGHTS", lights)
    data <- rbind(power, lights)

    ## Build the load data plot
    require(ggplot2)
    require(scales)
    gg <- ggplot(data, aes(x=datetime, y=value/1000)) +
        geom_area(aes(fill=id), position="stack") +
            scale_x_datetime(label=date_format("%H:%M")) +
                labs(x="Hour of the day", y="Power (kW)", fill="Load") +
                    theme_bw() 

    ## Get the size of this plot
    ## http://stackoverflow.com/questions/7705345/how-can-i-extract-plot-axes-ranges-for-a-ggplot2-object
    gg.data <- ggplot_build(gg)
    ylim <- gg.data$panel$ranges[[1]]$y.minor_source
    ylim <- ylim[c(1, length(ylim))]

    ## This can be used to build the occupancy scaling
    occupancy <- get_occupancy_data(dir)
    
    ## Now transform this to get the occupancy windows
    occupancy <- transform(occupancy, occ=rescale(occupants, ylim))
   
    ## Now add this to the plot in the form of a grey background
    newlayer <- geom_area(data=occupancy, aes(x=datetime, y=occ), fill="#999999", alpha=0.5)
    gg <- insertLayer(gg, after=0, newlayer)

    ggsave(file, gg, width=10, height=6)
}

##' Inserts a new layer into a ggplot object at a specified order
##'
##' @param P the original ggplot object
##' @param after the position where to insert new layers, relative to
##' existing layers
##' @param ... additional layers to insert, separated by commas
##' instead of plus signs
##'
##' @seealso
##' http://stackoverflow.com/questions/20249653/insert-layer-underneath-existing-layers-in-ggplot2-object
insertLayer <- function(P, after=0, ...) {
    
      if (after < 0)
        after <- after + length(P$layers)

      if (!length(P$layers))
        P$layers <- list(...)
      else 
        P$layers <- append(P$layers, list(...), after)

      return(P)
    }
