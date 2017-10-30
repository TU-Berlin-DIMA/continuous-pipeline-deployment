setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/criteo-full/")
library(ggplot2)
library(reshape)

library(gridExtra)
library(grid)

cUpdate = sum(read.csv('training-time/local/continuous/update', header = FALSE, col.names = c('c_update')))/1000
cTransform = sum(read.csv('training-time/local/continuous/transform', header = FALSE, col.names = c('c_transform')))/1000
cTrain = sum(read.csv('training-time/local/continuous/train', header = FALSE, col.names = c('c_train')))/1000

pTotal = sum(read.csv('training-time/local/periodical/total', header = FALSE, col.names = c('p_total')))/1000

deploymentTypes = data.frame(types = c('Continuous', 'Periodical'),time =  c(cTrain+cTransform+cUpdate,  pTotal))

deploymentTypesPlot = 
  ggplot(data = deploymentTypes) + 
  geom_bar(aes(x = types, weight = time), fill = c("#00aedb","#d11141"),color =c('#000000', '#000000'),
           width = 1) + 
  theme_bw() + 
  xlab("") + 
  ylab("Total Training Time (s)") + 
  theme(axis.text=element_text(size=25, color = "black"),
        axis.title=element_text(size=25, color= "black"),
        legend.position = "none",
        panel.border = element_rect(colour = "black", fill=NA, size=3)) 

ggsave(deploymentTypesPlot , filename = 'training-time/local/criteo-training-time-deployment-types-experiment.eps', 
       device = 'eps', 
       width = 12, height = 6, 
       units = "in")

optimizations = data.frame(types = factor(c('No Optimzation','Statistics Update', 'Materialization'), 
                                            levels = c('No Optimzation','Statistics Update', 'Materialization')), 
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
  ylab("Total Training Time (s)") + 
  theme(axis.text=element_text(size=25, color = "black"),
        axis.title=element_text(size=25, color= "black"),
        panel.border = element_rect(colour = "black", fill=NA, size=3)) 

ggsave(optimizationsPlot , filename = 'training-time/local/criteo-training-time-optimizations-experiment.eps', 
       device = 'eps', 
       width = 12, height = 6, 
       units = "in")
