## Plot results of occupancy model results
## James Keirstead
## 22 January 2014

get_occupancy_data <- function(dir) {
    ## Load the raw data
    data <- read.csv(file.path(dir, "occupancy_output.csv"), header=FALSE, stringsAsFactors=FALSE)
    names(data) <- c("tid", "occupants")

    ## Tidy up the time stamps
    tmp <- data.frame(tid=1:144, datetime=seq(as.POSIXct("2014-01-01 00:00:00"),
                                  as.POSIXct("2014-01-01 23:59:00"), by="10 min"))

    data <- merge(data, tmp, by="tid")
    data <- data[,-1]
    data <- data[,c("datetime", "occupants")]
    return(data)
}

make_occupancy_plot <- function(dir) {
    data <- get_occupancy_data(dir)
    gg <- ggplot(data, aes(x=datetime, y=occupants)) + geom_line() +
        labs(x="Hour of the day", y="Active occupants") +
            scale_x_datetime(label=date_format("%H:%M")) +
                theme_bw()

    ggsave("occupancy.png", gg)
}
