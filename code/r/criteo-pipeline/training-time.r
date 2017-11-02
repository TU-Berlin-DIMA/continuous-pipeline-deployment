setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/criteo-full/")
library(ggplot2)
library(reshape)

library(gridExtra)
library(grid)

#local
#cUpdate = sum(read.csv('training-time/local/continuous/100/update', header = FALSE, col.names = c('c_update')))/1000
#cTransform = sum(read.csv('training-time/local/continuous/100/transform', header = FALSE, col.names = c('c_transform')))/1000
#cTrain = sum(read.csv('training-time/local/continuous/100/train', header = FALSE, col.names = c('c_train')))/1000
#pTotal = sum(read.csv('training-time/local/periodical/total', header = FALSE, col.names = c('p_total')))/1000

#cluster
cUpdate = sum(read.csv('training-time/cluster/continuous/1440/update', header = FALSE, col.names = c('c_update')))/60000
cTransform = sum(read.csv('training-time/cluster/continuous/1440/transform', header = FALSE, col.names = c('c_transform')))/60000
cTrain = sum(read.csv('training-time/cluster/continuous/1440/train', header = FALSE, col.names = c('c_train')))/60000
pTotal = sum(read.csv('training-time/cluster/periodical/total', header = FALSE, col.names = c('p_total')))/60000

deploymentTypes = data.frame(types = c('Continuous', 'Periodical'),time =  c(cTrain+cTransform+cUpdate,  pTotal))

deploymentTypesPlot = 
  ggplot(data = deploymentTypes) + 
  geom_bar(aes(x = types, weight = time), fill = c("#00aedb","#d11141"),color =c('#000000', '#000000'),
           width = 1) + 
  theme_bw() + 
  xlab("") + 
  ylab("Time (m)") + 
  theme(axis.text=element_text(size=60, color = "black"),
        axis.title=element_text(size=60, color= "black"),
        axis.text.x = element_text(size = 60, color ="black", angle = 45,vjust = 0.8, hjust = 0.8),
        legend.position = "none",
        panel.border = element_rect(colour = "black", fill=NA, size=3)) 

optimizations = data.frame(types = factor(c('No Opt','Stat Update', 'Materialized'), 
                                            levels = c('No Opt','Stat Update', 'Materialized')), 
                             time = c(cTrain+cTransform+cUpdate,
                                      cTrain+cTransform, 
                                      cTrain))


optimizationsPlot = 
  ggplot(data = optimizations) + 
  geom_bar(aes(x = types, weight = time),
           colour = '#000000', 
           fill = '#00aedb',
           width = 1) + 
  theme_bw() + 
  xlab("") + 
  ylab("") + 
  theme(axis.text=element_text(size=60, color = "black"),
        axis.text.x = element_text(size = 60, color ="black", angle = 45, vjust = 0.8, hjust = 0.8),
        axis.title=element_text(size=60, color= "black"),
        panel.border = element_rect(colour = "black", fill=NA, size=3)) 

#local
#ggsave(deploymentTypesPlot , filename = 'training-time/local/criteo-training-time-deployment-types-experiment.eps', device = 'eps', width = 12, height = 6,    units = "in")
#ggsave(optimizationsPlot , filename = 'training-time/local/criteo-training-time-optimizations-experiment.eps', device = 'eps', width = 12, height = 6, units = "in")

#cluster
ggsave(deploymentTypesPlot , filename = 'training-time/cluster/criteo-training-time-deployment-types-experiment.eps', device = 'eps', width = 12, height = 12, units = "in")
ggsave(optimizationsPlot , filename = 'training-time/cluster/criteo-training-time-optimizations-experiment.eps', device = 'eps', width = 12, height = 12, units = "in")