setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/criteo-full/")
library(ggplot2)
library(reshape)

tHistory = read.csv('sampling-mode-time/cluster/entire-history/time', header = FALSE, col.names = c('time_total'))
tOneDay = read.csv('sampling-mode-time/cluster/1440/time', header = FALSE, col.names = c('time_total'))
tHalfDay = read.csv('sampling-mode-time/cluster/720/time', header = FALSE, col.names = c('time_total'))
tNoSample = read.csv('sampling-mode-time/cluster/no-sampling/time', header = FALSE, col.names = c('time_total'))

dHistory = read.csv('sampling-mode-time/cluster/entire-history/data-processing', header = FALSE, col.names = c('data_processing'))
dOneDay = read.csv('sampling-mode-time/cluster/1440/data-processing', header = FALSE, col.names = c('data_processing'))
dHalfDay = read.csv('sampling-mode-time/cluster/720/data-processing', header = FALSE, col.names = c('data_processing'))
dNoSample = read.csv('sampling-mode-time/cluster/no-sampling/data-processing', header = FALSE, col.names = c('data_processing'))

times = data.frame(types = factor(c('No Sampling','Half Day','One Day','Entire History'),
                                  levels = c('No Sampling','Half Day','One Day','Entire History')),
                                  total_time =c(tNoSample$time_total,tHalfDay$time_total,tOneDay$time_total,tHistory$time_total), 
                   data_processing = c(dNoSample$data_processing,dHalfDay$data_processing,dOneDay$data_processing,dHistory$data_processing))

times$total_time = times$total_time / 62256
times$data_processing = times$data_processing / 61773


plotTotalTime = 
  ggplot(data = times,aes(x = types, y = total_time)) +
  geom_bar(stat = 'identity',width = 1,colour = '#000000', fill = '#00aedb') +
  geom_text(aes(label = sprintf("%0.2f", round(total_time, digits = 2))),position = position_dodge(width = 1),  vjust = 1.2, size = 20) + 
  theme_bw() + 
  xlab("") + 
  ylab("Slow Down Factor") + 
  theme(axis.text=element_text(size=60, color = "black"),
        axis.title=element_text(size=60, color= "black"),
        axis.text.x = element_text(size = 60, color ="black", angle = 45, vjust = 0.7, hjust = 0.7),
        legend.position = "none",
        panel.border = element_rect(colour = "black", fill=NA, size=3)) 


plotProcessingTime = 
  ggplot(data = times,aes(x = types, y = data_processing)) +
  geom_bar(stat = 'identity',width = 1,colour = '#000000', fill = '#d11141') +
  geom_text(aes(label = sprintf("%0.2f", round(data_processing, digits = 2))),position = position_dodge(width = 1),  vjust = 1.2,size = 20) + 
  theme_bw() + 
  xlab("") + 
  ylab("") + 
  theme(axis.text=element_text(size=60, color = "black"),
        axis.title=element_text(size=60, color= "black"),
        axis.text.x = element_text(size = 60, color ="black", angle = 45, vjust = 0.7, hjust = 0.7),
        legend.position = "none",
        panel.border = element_rect(colour = "black", fill=NA, size=3)) 


ggsave(plotTotalTime , filename = 'sampling-mode-time/cluster/criteo-sampling-total-experiment.eps', 
       device = 'eps', 
       width = 12, height = 12, 
       units = "in")

ggsave(plotProcessingTime , filename = 'sampling-mode-time/cluster/criteo-sampling-data-experiment.eps', 
       device = 'eps', 
       width = 12, height = 12, 
       units = "in")
