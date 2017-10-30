setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/criteo-full/")
library(ggplot2)
library(reshape)

library(gridExtra)
library(grid)

cont = read.csv('quality/local/continuous/loss', header = FALSE, col.names = c('continuous'))
period = read.csv('quality/local/periodical/loss', header = FALSE, col.names = c('periodical'))
periodical = data.frame(time = c(1,21,41,61,81,101), periodical = period)
continuous = data.frame(time = 1:nrow(cont), continuous = cont$continuous)

groupColors <- c(continuous = "#00aedb", periodical = "#d11141")

ml = melt(continuous, id.vars = c('time') )
mlp =  melt(periodical, id.vars = c('time') )
ggplot() + 
  geom_line(data = ml, aes(x = time, y = value, colour = variable), size = 1.4) +
  geom_point(data = mlp, aes(x = time, y = value, colour = variable), shape = 16, size = 5) +
  theme_bw() + 
  xlab("") + 
  ylab("Total Training Time (s)") + 
  scale_color_manual(values = groupColors,  guide = guide_legend(override.aes = list(shape = c(NA, 16), linetype = c("solid","blank")))) + 
  theme(legend.text = element_text(size = 20, color = "black"), 
        axis.text=element_text(size=20, color = "black"),
        axis.title=element_text(size=20, color= "black"),
        legend.key.width = unit(3, "cm"), 
        legend.key.height = unit(0.8, "cm"),
        legend.position = "bottom",
        legend.title = element_blank(),
        panel.border = element_rect(colour = "black", fill=NA, size=3)) 


  