## Plot results of lighting model
## James Keirstead
## 22 January 2014

data <- read.csv("../lighting_output.csv", header=FALSE, stringsAsFactors=FALSE)
names(data) <- c("id", "rating", "weight", 1:1440)
data.m <- melt(data, id="id", variable.name="tid")

tmp <- data.frame(tid=1:1440, datetime=seq(as.POSIXct("2014-01-01 00:00:00"),
                                  as.POSIXct("2014-01-01 23:59:00"), by="1 min"))

data.m <- merge(data.m, tmp, by="tid")
data.m <- ddply(data.m, .(datetime), summarize, value=sum(value))
data.m <- arrange(data.m, datetime)

require(scales)
gg <- ggplot(data.m, aes(x=datetime, y=value)) + geom_line() +
    labs(x="Hour of the day", y="W") +
    scale_x_datetime(label=date_format("%H:%M")) +
    theme_bw()

ggsave("lighting.png", gg)


## Plot irradiance data
irradiance <- read.csv("../src/data/irradiance.csv", header=FALSE, skip=8, stringsAsFactors=FALSE)
month <- 1
df <- data.frame(time=irradiance$V1, irradiance=irradiance[,2+month])
gg <- ggplot(df, aes(x=time, y=irradiance)) +
    geom_line() +
    labs(x="Minute of the day", y="Irradiance (W/m2")


ggsave("irradiance.png", gg)
