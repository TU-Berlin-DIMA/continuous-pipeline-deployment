setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/criteo-full/")
library(ggplot2)
library(reshape)

library(gridExtra)
library(grid)

cont = read.csv('quality/local/continuous/loss_100', header = FALSE, col.names = c('Continuous'))
period = read.csv('quality/local/periodical/loss_', header = FALSE, col.names = c('Periodical'))
periodical = data.frame(time = c(0,26,51,76, 101, 126), periodical = period)
continuous = data.frame(time = 1:nrow(cont), Continuous = cont$Continuous)

groupColors <- c(Continuous = "#00aedb", Periodical = "#d11141")

ml = melt(continuous, id.vars = c('time') )
mlp =  melt(periodical, id.vars = c('time') )
plot = 
  ggplot() + 
  geom_line(data = ml, aes(x = time, y = value, colour = variable), size = 1.4) +
  geom_point(data = mlp, aes(x = time, y = value, colour = variable), shape = 16, size = 5) +
  theme_bw() + 
  xlab("") + 
  scale_x_continuous(name ="Time",
                     breaks = c(0,20,40,60, 80,100),
                     labels=c("Deployment","Day 1","Day 2","Day 3", "Day 4","Day 5")) +
  scale_color_manual(values = groupColors, 
                     labels = groupNames, 
                     guide = guide_legend(override.aes = list(shape = c(NA,16), linetype = c("solid","blank")))) +
  theme(legend.text = element_text(size = 25, color = "black"), 
        axis.text=element_text(size=25, color = "black"),
        axis.title=element_text(size=25, color= "black"),
        legend.key.width = unit(2, "cm"), 
        legend.key.height = unit(0.8, "cm"),
        legend.position = "bottom",
        legend.title = element_blank(),
        panel.border = element_rect(colour = "black", fill=NA, size=3)) 


ggsave(plot , filename = 'quality/local/criteo-proactive-training-experiment.eps', 
       device = 'eps', 
       width = 12, height = 6, 
       units = "in")
