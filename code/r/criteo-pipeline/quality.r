setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/criteo-full/")
library(ggplot2)
library(reshape)

library(gridExtra)
library(grid)

cont = read.csv('quality/cluster/continuous/loss_0', header = FALSE, col.names = c('Continuous'))
periodical = read.csv('quality/cluster/periodical/loss_1', header = FALSE, col.names = c('day','iter','Periodical'))
continuous = data.frame(time = 1:nrow(cont), Continuous = cont$Continuous)

groupColors <- c(Continuous = "#00aedb", Periodical = "#d11141")


ml = melt(continuous, id.vars = c('time'))
plot = 
  ggplot() + 
  geom_line(data = ml, aes(x = time, y = value, colour = variable), size = 1.4) +
  geom_point(data = periodical[periodical$day == 145, ], aes(x = day, y = Periodical, colour = 'Periodical'), size = 5) +
  geom_text( data = periodical[periodical$day == 145, ], aes(x = day, y = Periodical, label = iter),position = position_dodge(width = 1),  hjust = 1.1,  size = 10) +
  geom_point(data = periodical[periodical$day == 289, ], aes(x = day, y = Periodical, colour = 'Periodical'), size = 5) +
  geom_text( data = periodical[periodical$day == 289, ], aes(x = day, y = Periodical, label = iter),position = position_dodge(width = 1),  hjust = 1.1,  size = 10) +
  theme_bw() +
  xlab("") + 
  ylim(c(0.132,0.1375))+
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
