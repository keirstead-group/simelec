## Plot results of lighting model
## James Keirstead
## 22 January 2014

get_lighting_data <- function(dir) {
    ## Load the data
    data <- read.csv(file.path(dir, "lighting_output.csv"), header=FALSE, stringsAsFactors=FALSE)
    names(data) <- c("id", "rating", "weight", 1:1440)
    data.m <- melt(data, id="id", variable.name="tid")

    ## Tidy up the time stamps
    tmp <- data.frame(tid=1:1440, datetime=seq(as.POSIXct("2014-01-01 00:00:00"),
                                  as.POSIXct("2014-01-01 23:59:00"), by="1 min"))

    data.m <- merge(data.m, tmp, by="tid")
    ## Calculate the total light demand
    data.m <- ddply(data.m, .(datetime), summarize, value=sum(value))

    ## Sort by time and return the result    
    data.m <- arrange(data.m, datetime)
    return(data.m)
}

make_lighting_plot <- function(dir) {
    data.m <- get_lighting_data(dir)

    require(scales)
    gg <- ggplot(data.m, aes(x=datetime, y=value)) + geom_line() +
        labs(x="Hour of the day", y="W") +
            scale_x_datetime(label=date_format("%H:%M")) +
                theme_bw()

    ggsave("lighting.png", gg)
}


