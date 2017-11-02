setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/criteo-full/")
library(ggplot2)
library(reshape)

library(gridExtra)
library(grid)

cont = read.csv('quality/cluster/continuous/loss_0', header = FALSE, col.names = c('Continuous'))
periodical = read.csv('quality/cluster/periodical/loss_1', header = FALSE, col.names = c('day','iter','Periodical'))
continuous = data.frame(time = 1:nrow(cont), Continuous = cont$Continuous)

groupColors <- c(Continuous = "#00aedb", Periodical500 = "#d11141", Periodical1000 = "#f37735",Periodical2000="#007f00" )
groupShapes <- c(Continuous = NA, Periodical500 = 15, Periodical1000 = 17,Periodical2000=19 )

ml = melt(continuous, id.vars = c('time'))
plot = 
  ggplot() + 
  geom_line(data = ml, aes(x = time, y = value, colour = variable), size = 1.4) +
  geom_point(data = periodical[periodical$iter == 'Periodical500',], aes(x = day, y = Periodical, colour = 'Periodical500', shape = 'Periodical500') ,size = 10) +
  geom_point(data = periodical[periodical$iter == 'Periodical1000',], aes(x = day, y = Periodical, colour = 'Periodical1000',shape = 'Periodical1000'), size = 10) +
  geom_point(data = periodical[periodical$iter == 'Periodical2000',], aes(x = day, y = Periodical, colour = 'Periodical2000',shape = 'Periodical2000'), size = 10) +
  theme_bw() +
  ylab("Logistic Error") + 
  ylim(c(0.132,0.137))+
  scale_color_manual("", values=groupColors, guide = guide_legend(override.aes = list(linetype =c("solid","blank","blank","blank")))) +
  theme(legend.text = element_text(size = 25, color = "black"), 
        axis.text=element_text(size=30, color = "black"),
        axis.title=element_text(size=30, color= "black"),
        legend.key.width = unit(2, "cm"), 
        legend.key.height = unit(1, "cm"),
        legend.key = element_rect(fill=alpha('blue',0.0)),
        legend.position = c(0.18,0.85),
        legend.direction = 'vertical',
        legend.title = element_blank(),
        legend.background = element_rect(fill=alpha('blue', 0.0)),
        panel.border = element_rect(colour = "black", fill=NA, size=3)) +
  scale_x_continuous(name ="Time",
                     breaks = c(1,145,289),
                     labels=c("Deployment","Day 1","Day 2")) 


ggsave(plot , filename = 'quality/cluster/criteo-proactive-training.pdf', 
       device = 'pdf', 
       width = 12, height = 6, 
       units = "in")
