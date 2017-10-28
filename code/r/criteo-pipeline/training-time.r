setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/criteo-full/")
library(ggplot2)
library(reshape)

library(gridExtra)
library(grid)

contNoOpt = sum(read.csv('training-time/local/continuous-no-opt/time', header = FALSE, col.names = c('contNoOpt')))/1000
contStatUpdate = sum(read.csv('training-time/local/continuous-stat-update/time', header = FALSE, col.names = c('contStatUpdate')))/1000
periodNoUpt = sum(read.csv('training-time/local/periodical-no-opt/time', header = FALSE, col.names = c('periodNoUpt')))/1000




deploymentTypes = data.frame(types = c('Continuous No Opt','Continuous Stat Update', 'Periodical No Opt'), time = c(contNoOpt,contStatUpdate,periodNoUpt))



finalPlot = ggplot(data = deploymentTypes) + 
  geom_bar(aes(x = types, weight = time),
           fill = "#00aedb", 
           width = 0.5, 
           color = "#00aedb") + 
  theme_bw() + 
 
  xlab("Deployment Approach") + 
  ylab("Total Training Time (s)") + 
  theme(axis.text=element_text(size=22, color = "black"),
        axis.title=element_text(size=22, color= "black"),
        panel.border = element_rect(colour = "black", fill=NA, size=3)) 

ggsave(finalPlot , filename = 'training-time/local/training-time-experiment.eps', 
       device = 'eps', 
       width = 10, height = 10, 
       units = "in")
