setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/criteo-full/")
library(ggplot2)
library(reshape)

library(gridExtra)
library(grid)

getLabel <- function(l) {
  v = l/1000
  f = ifelse(v < 1000 , (sprintf("%.0f", v)),  (sprintf("%.f M", v/1000)) )
  return (f)
}

features = read.csv('feature-discovery/cluster/counts', header = FALSE, col.names = c('DayCounts'))

dailySize = data.frame(days = 1:6, DayCounts = features)

finalPlot =
  ggplot(data = dailySize) + 
  geom_bar(aes(x = days, weight = DayCounts),
           fill = "#00aedb", 
           colour = '#000000',
           width = 1) + 
  geom_text(
    aes(x = days, y = DayCounts, label = getLabel(DayCounts)),
    position = position_dodge(width = 1),
    vjust = -1, 
    size = 9) +
  #geom_point(data = dailySize, aes(x = dayIndex, y = DayCounts)) + 
  theme_bw() + 
  scale_x_continuous(name ="\nTime",
                     breaks = c(1,2,3,4,5,6),
                     labels=c("Deployment","Day 1","Day 2","Day 3","Day 4","Day 5")) +
  scale_y_continuous(limits = c(0,dailySize[6,2] + 50000000), 
                     labels = getLabel) + 
  xlab("Time") + 
  ylab("Feature Size") + 
  theme(axis.text=element_text(size=28, color = "black"),
        axis.title=element_text(size=28, color= "black"),
        panel.border = element_rect(colour = "black", fill=NA, size=3)) 

ggsave(finalPlot , filename = 'feature-discovery/cluster/criteo-feature-discovery-experiment.eps', 
       device = 'eps', 
       width = 12, height = 6, 
       units = "in")
