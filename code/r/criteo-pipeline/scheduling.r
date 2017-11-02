setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/criteo-full/")
library(ggplot2)
library(reshape)

library(zoo)

#cluster
update = read.csv('training-time/cluster/continuous/1440/update', header = FALSE, col.names = c('update'))$update/1000
transform = read.csv('training-time/cluster/continuous/1440/transform', header = FALSE, col.names = c('transform'))$transform/1000
train = read.csv('training-time/cluster/continuous/1440/train', header = FALSE, col.names = c('train'))$train/1000

proactives = data.frame (ind = 1:288, time = rollapply(update, width = 10, sum, by = 10) + transform + train)

average = data.frame(time = mean(proactives$time))

groupColors <- c(proactive = "#00aedb", average_proactive = "#d11141",scheduled_proactive= '#f37735' )
groupNames <- c(proactive = "Proactive", average_proactive = "Proactive (AVG)", scheduled_proactive ="Scheduling Intervals")

SLACK = 10
prxpl = 15/60
scheduled_proactive = data.frame(ind =  c(proactives$ind, 289), time = c(NA,SLACK * (proactives$time * prxpl))) 


plot = 
  ggplot() + 
  geom_line(data = proactives, aes(x = ind, y = time, colour = 'proactive'), size = 1.4) +
  geom_hline(data = average, aes(yintercept = time, colour = 'average_proactive'), size = 1.4) +
  geom_line(data = scheduled_proactive, aes(x = ind, y = time, colour = 'scheduled_proactive'), size = 1.4) +
  theme_bw() +
  ylab("Time (s)") +
  xlab("") + 
  scale_x_continuous(name ="Time",
                     breaks = c(1,145,289),
                     labels=c("Deployment","Day 1","Day 2")) +
  scale_color_manual(values = groupColors, labels = groupNames) +
  theme(legend.text = element_text(size = 28, color = "black"), 
        axis.text=element_text(size=30, color = "black"),
        axis.title=element_text(size=30, color= "black"),
        legend.key.width = unit(2, "cm"), 
        legend.key.height = unit(0.8, "cm"),
        legend.position = "bottom",
        legend.title = element_blank(),
        panel.border = element_rect(colour = "black", fill=NA, size=3)) 


ggsave(plot , filename = 'scheduling/cluster/criteo-scheduling-experiment.eps', 
       device = 'eps', 
       width = 12, height = 6, 
       units = "in")



