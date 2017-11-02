setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/criteo-full/")
library(ggplot2)
library(reshape)

library(gridExtra)
library(grid)


# local
#adam = read.csv('learning-rate/local/adam', header = FALSE, col.names = c('adam'))
#rmsprop = read.csv('learning-rate/local/rmsprop', header = FALSE, col.names = c('rmsprop'))
#momentum = read.csv('learning-rate/local/momentum', header = FALSE, col.names = c('momentum'))
#adadelta = read.csv('learning-rate/local/adadelta', header = FALSE, col.names = c('adadelta'))

#cluster
adam = read.csv('learning-rate/cluster/adam/loss', header = FALSE, col.names = c('iter','Adam'))
rmsprop = read.csv('learning-rate/cluster/rmsprop/loss', header = FALSE, col.names = c('iter','RMSprop'))
#momentum = read.csv('learning-rate/cluster/momentum/loss', header = FALSE, col.names = c('iter','momentum'))
adadelta = read.csv('learning-rate/cluster/adadelta/loss', header = FALSE, col.names = c('iter','Adadelta'))


adam_d = read.csv('learning-rate/cluster/adam/loss_0', header = FALSE, col.names = c('Adam'))
rmsprop_d = read.csv('learning-rate/cluster/rmsprop/loss_1440', header = FALSE, col.names = c('RMSprop'))
#momentum = read.csv('learning-rate/cluster/momentum/loss', header = FALSE, col.names = c('momentum'))
adadelta_d = read.csv('learning-rate/cluster/adadelta/loss_1440', header = FALSE, col.names = c('Adadelta'))


groupColors <- c(Adam = "#d11141", RMSprop = "#00b159", Adadelta = "#f37735")
training = data.frame(index = c(1,25,51,76,101,125,seq(127,291,by = 1)),
                      Adam = c(adam$Adam,rep(NA,20),adam_d$Adam),
                      RMSprop = c(rmsprop$RMSprop,rep(NA,20),rmsprop_d$RMSprop),
                      #momentum = momentum$momentum,
                      Adadelta = c(adadelta$Adadelta,rep(NA,20),adadelta_d$Adadelta))

ml = melt(training, id.vars = c('index') )
plot_training = 
  ggplot(data = NULL) + 
  geom_line(data = ml ,aes( x = index, y = value, colour = variable, group = variable), size = 1.4) + 
  geom_point(data = ml[ml$index< 126, ], aes( x = index, y = value,color = variable, group = variable), size = 3.0) + 
  geom_text( aes(x = 133, y = 0.40, label = "DEPLOYMENT"), position = position_dodge(width = 1),angle = -90, vjust = -0.05, size = 10) +
  geom_rect(aes(xmin=126, xmax=147, ymin=0.1, ymax=0.72), alpha=0.5) + 
  ylab("Logistic Loss") + 
  theme_bw() + 
  scale_color_manual(values = groupColors,  guide = guide_legend(override.aes = list(shape = c(NA,NA, NA)))) + 
  scale_x_discrete(name ="Iterations                    Time of Day",
                   limits =c(1,25,51,76,101,126,174,214,255), labels=c("20","40","80","160","320","500","06:00","12:00","18:00")) + 
  theme(legend.text = element_text(size = 28, color = "black"), 
        axis.text=element_text(size=28, color = "black"),
        axis.title=element_text(size=28, color= "black"),
        legend.key.width = unit(3, "cm"), 
        legend.key.height = unit(0.8, "cm"),
        legend.position = "bottom",
        legend.title = element_blank(),
        panel.border = element_rect(colour = "black", fill=NA, size=3)) 


ggsave(plot_training , filename = 'learning-rate/cluster/criteo-learning-rate-experiment.pdf', 
       device = 'pdf', 
       width = 12, height = 6, 
       units = "in")
