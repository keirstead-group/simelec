## Plot results of appliance model
## James Keirstead
## 23 January 2014

##' Gets the appliance data in the form of a melted data.frame
##'
##' @param dir the directory containing the output file
##' @return a data frame
get_appliance_data <- function(dir) {

    ## Load the data
    data <- read.csv(file.path(dir, "appliance_output.csv"), header=FALSE, stringsAsFactors=FALSE)
    names(data) <- c("id", 1:1440)
    data.m <- melt(data, id="id", variable.name="tid")

    ## Tidy up the dates
    tmp <- data.frame(tid=1:1440, datetime=seq(as.POSIXct("2014-01-01 00:00:00"),
                                  as.POSIXct("2014-01-01 23:59:00"), by="1 min"))

    data.m <- merge(data.m, tmp, by="tid")

    ## Remove all of the empty demands
    tmp <- ddply(data.m, .(id), summarize, total=sum(value))
    bad_id <- subset(tmp, total==0)$id
    data.m <- subset(data.m, !is.element(id, bad_id))
    data.m <- data.m[,-1] ## Drop tid column
    data.m <- data.m[,c("id", "datetime", "value")]
    
    ## Arrange in date order and return
    data.m <- arrange(data.m, datetime)
    return(data.m)
}

make_appliances_plot <- function(dir) {
    data <- get_appliance_data(dir)
    require(scales)
    gg <- ggplot(data.m, aes(x=datetime, y=value)) + geom_area(aes(fill=id), position="stack") +
        labs(x="Hour of the day", y="W") +
            scale_x_datetime(label=date_format("%H:%M")) +
                theme_bw()

    ggsave("appliances.png", gg)
}

