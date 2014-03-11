## Plot results of appliance model
## James Keirstead
## 23 January 2014

data <- read.csv("../appliance_output.csv", header=FALSE, stringsAsFactors=FALSE)
names(data) <- c("id", 1:1440)
data.m <- melt(data, id="id", variable.name="tid")

tmp <- data.frame(tid=1:1440, datetime=seq(as.POSIXct("2014-01-01 00:00:00"),
                                  as.POSIXct("2014-01-01 23:59:00"), by="1 min"))

data.m <- merge(data.m, tmp, by="tid")

## Remove all of the empty demands
tmp <- ddply(data.m, .(id), summarize, total=sum(value))
bad_id <- subset(tmp, total==0)$id
data.m <- subset(data.m, !is.element(id, bad_id))

data.m <- arrange(data.m, datetime)
require(scales)
gg <- ggplot(data.m, aes(x=datetime, y=value)) + geom_area(aes(fill=id), position="stack") +
    labs(x="Hour of the day", y="W") +
    scale_x_datetime(label=date_format("%H:%M")) +
    theme_bw()

ggsave("appliances.png", gg)
