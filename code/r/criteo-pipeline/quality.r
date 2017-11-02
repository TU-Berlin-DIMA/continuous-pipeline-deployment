setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/criteo-full/")
library(ggplot2)
library(reshape)

library(gridExtra)
library(grid)

cont = read.csv('quality/cluster/continuous/loss_0', header = FALSE, col.names = c('Continuous'))
period = read.csv('quality/cluster/periodical/loss_1', header = FALSE, col.names = c('iter','Periodical'))
periodical = data.frame(time = c(1,145,289), iter = period$iter, Periodical = period$Periodical)
continuous = data.frame(time = 1:nrow(cont), Continuous = cont$Continuous)

groupColors <- c(Continuous = "#00aedb", Periodical = "#d11141")


ml = melt(continuous, id.vars = c('time'))
mlp = melt(periodical, id.vars = c('time','iter') )
plot = 
  ggplot() + 
  geom_line(data = ml, aes(x = time, y = value, colour = variable), size = 1.4) +
  geom_point(data = mlp, aes(x = 145, y = value, colour = variable), size = 5) +
  geom_text( data = mlp, aes(x = 145, y = value, label = iter), position = position_dodge(width = 1),  hjust = 0.5, vjust=-1,  size = 10) +
  theme_bw() +
  xlab("") + 
  scale_x_continuous(name ="\nTime",
                     breaks = c(1,145,289),
                     labels=c("Deployment","Day 1","Day 2")) +
  scale_color_manual(values = groupColors, 
                     guide = guide_legend(override.aes = list(shape = c(NA,16), linetype = c("solid","blank")))) +
  theme(legend.text = element_text(size = 30, color = "black"), 
        axis.text=element_text(size=30, color = "black"),
        axis.title=element_text(size=30, color= "black"),
        legend.key.width = unit(2, "cm"), 
        legend.key.height = unit(0.8, "cm"),
        legend.position = "bottom",
        legend.title = element_blank(),
        panel.border = element_rect(colour = "black", fill=NA, size=3)) 


ggsave(plot , filename = 'quality/cluster/criteo-proactive-training-experiment.eps', 
       device = 'eps', 
       width = 12, height = 6, 
       units = "in")
