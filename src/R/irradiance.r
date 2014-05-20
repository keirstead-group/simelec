## Plot irradiance data for a specified month
## James Keirstead

simelec_dir <- "../src/data/"
month <- 1

irradiance <- read.csv(file.path(simelec_dir, "irradiance.csv"), header=FALSE, skip=8, stringsAsFactors=FALSE)
df <- data.frame(time=irradiance$V1, irradiance=irradiance[,2+month])
gg <- ggplot(df, aes(x=time, y=irradiance)) +
    geom_line() +
    labs(x="Minute of the day", y="Irradiance (W/m2")

ggsave("irradiance.png", gg)
