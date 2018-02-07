setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/criteo-full/")
library(ggplot2)
library(reshape)


# local prequential
entireHistory = read.csv('sampling-mode/local-prequential/continuous/loss_-1', header = FALSE, col.names = c('e','s'))
oneDay = read.csv('sampling-mode/local-prequential/continuous/loss_100', header = FALSE, col.names = c('e','s'))
halfDay = read.csv('sampling-mode/local-prequential/continuous/loss_50', header = FALSE, col.names = c('e','s'))
noSampling = read.csv('sampling-mode/local-prequential/continuous/loss_0', header = FALSE, col.names = c('e','s'))
breaks = c(1,101,201,301,401,501)
labels = c("Deployment","Day 1","Day 2","Day 3","Day 4","Day 5")

entireHistory = cumsum(entireHistory)
oneDay = cumsum(oneDay)
halfDay = cumsum(halfDay)
noSampling = cumsum(noSampling)


entireHistory$entireHistory = entireHistory$e / entireHistory$s
oneDay$oneDay = oneDay$e / oneDay$s
halfDay$halfDay = halfDay$e / halfDay$s
noSampling$noSampling = noSampling$e / noSampling$s

df = data.frame(time = 1:nrow(entireHistory),
                entireHistory = entireHistory$entireHistory,
                oneDay = oneDay$oneDay,
                halfDay = halfDay$halfDay,
                noSampling = noSampling$noSampling)

groupColors <- c(entireHistory = "#d11141", oneDay = "#00b159", halfDay ="#00aedb", noSampling = "#f37735")
groupNames <- c(entireHistory = "Entire History", oneDay = "One Day", halfDay ="Half Day", noSampling = "No Sampling")
ml = melt(df, id.vars = 'time')
pl = 
  ggplot(data = ml, aes(x = time, y = value, group = variable)) + 
  geom_line(aes( colour = variable), size = 1.6) + 
  ylab("Logistic Loss") + 
  theme_bw() + 
  scale_x_continuous(name ="Time",
                     breaks = breaks,
                     labels= labels) +
  scale_color_manual(values = groupColors, 
                     labels = groupNames,
                     guide = guide_legend(override.aes = list(shape = c(NA,NA, NA, NA)))) +
  theme(legend.text = element_text(size = 30, color = "black"), 
        axis.text=element_text(size=30, color = "black"),
        axis.title=element_text(size=30, color= "black"),
        legend.key.width = unit(2, "cm"), 
        legend.key.height = unit(0.8, "cm"),
        legend.position = "bottom",
        legend.title = element_blank(),
        panel.border = element_rect(colour = "black", fill=NA, size=3))

lEntireHistory = df$entireHistory[nrow(entireHistory)]
lOneDay = df$oneDay[nrow(oneDay)]
lHalfDay = df$halfDay[nrow(halfDay)]
lNoSample = df$noSampling[nrow(noSampling)]

percentImprove <- function(a,b){
  ((a - b) * 100) / b
}
percentImprove(lEntireHistory,lOneDay)

percentImprove(lEntireHistory,lHalfDay)

percentImprove(lEntireHistory,lNoSample)
#local
ggsave(pl , filename = 'sampling-mode/local-prequential/criteo-sampling-mode-experiments.eps', device = 'eps', width = 12, height = 6, units = "in")