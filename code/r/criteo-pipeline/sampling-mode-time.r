setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/criteo-full/")
library(ggplot2)
library(reshape)

library(gridExtra)
library(grid)

getTotalTime <- function(root) {
  cUpdate = sum(read.csv(paste(root,sep='','/update'), header = FALSE, col.names = c('c_update')))/1000
  cTransform = sum(read.csv(paste(root,sep='','/transform'), header = FALSE, col.names = c('c_transform')))/1000
  cTrain = sum(read.csv(paste(root,sep='','/train'), header = FALSE, col.names = c('c_train')))/1000
  
  return (cUpdate + cTrain + cTransform)
}

entireHistory = getTotalTime('sampling-mode-time/local/continuous/-1')
oneDay = getTotalTime('sampling-mode-time/local/continuous/100')
halfDay = getTotalTime('sampling-mode-time/local/continuous/50')
noSampling = getTotalTime('sampling-mode-time/local/continuous/0')

times = data.frame(types = c('Entire History', 'One Day', 'Half Day','No Sampling' ),time =  c(entireHistory, oneDay, halfDay, noSampling))

groupNames <- c(entireHistory = "Entire History", oneDay = "One Day", halfDay ="Half Day", noSampling = "No Sampling")

ggplot(data = times) + 
  geom_bar(aes(x = types, weight = time),
           width = 1,
           colour = '#000000', 
           fill = '#00aedb') + 
  scale_color_manual(labels = groupNames) +
  theme_bw() + 
  xlab("") + 
  ylab("Total Training Time (s)") + 
  
  theme(axis.text=element_text(size=25, color = "black"),
        axis.title=element_text(size=25, color= "black"),
        legend.position = "none",
        panel.border = element_rect(colour = "black", fill=NA, size=3)) 