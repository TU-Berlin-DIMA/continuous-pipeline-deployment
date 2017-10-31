setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/criteo-full/")
library(ggplot2)
library(reshape)


# local
#entireHistory = read.csv('sampling-mode/local/continuous/loss_-1', header = FALSE, col.names = c('entireHistory'))
#oneDay = read.csv('sampling-mode/local/continuous/loss_100', header = FALSE, col.names = c('oneDay'))
#halfDay = read.csv('sampling-mode/local/continuous/loss_50', header = FALSE, col.names = c('halfDay'))
#noSampling = read.csv('sampling-mode/local/continuous/loss_0', header = FALSE, col.names = c('noSampling'))
#breaks = c(1,21,41,61,81,101)
#labels = c("Deployment","Day 1","Day 2","Day 3","Day 4","Day 5")

# cluster
entireHistory = read.csv('sampling-mode/cluster/continuous/loss_-1', header = FALSE, col.names = c('entireHistory'))
oneDay = read.csv('sampling-mode/cluster/continuous/loss_1440', header = FALSE, col.names = c('oneDay'))
halfDay = read.csv('sampling-mode/cluster/continuous/loss_720', header = FALSE, col.names = c('halfDay'))
noSampling = read.csv('sampling-mode/cluster/continuous/loss_0', header = FALSE, col.names = c('noSampling'))
breaks = c(1,145,289)
labels = c("Deployment","Day 1","Day 2")


df = data.frame(time = 1:nrow(entireHistory),
                entireHistory = entireHistory,
                oneDay = oneDay,
                halfDay = halfDay,
                noSampling = noSampling)

groupColors <- c(entireHistory = "#d11141", oneDay = "#00b159", halfDay ="#00aedb", noSampling = "#f37735")
groupNames <- c(entireHistory = "Entire History", oneDay = "One Day", halfDay ="Half Day", noSampling = "No Sampling")
ml = melt(df, id.vars = 'time')
pl = 
  ggplot(data = ml, aes(x = time, y = value, group = variable)) + 
  geom_line(aes( colour = variable), size = 1.6) + 
  ylab("Logistic Loss") + 
  theme_bw() + 
  scale_x_continuous(name ="\nTime",
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
#ggsave(pl , filename = 'sampling-mode/local/criteo-sampling-mode-experiments.eps', device = 'eps', width = 12, height = 6, units = "in")

#cluster
ggsave(pl , filename = 'sampling-mode/cluster/criteo-sampling-mode-experiments.eps', device = 'eps', width = 12, height = 6, units = "in")