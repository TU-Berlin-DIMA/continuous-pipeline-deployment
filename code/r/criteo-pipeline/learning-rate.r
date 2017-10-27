setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/criteo-full/")
library(ggplot2)
library(reshape)

library(gridExtra)
library(grid)

iterations = c(20,40,80,160,320, 500)

adam = read.csv('learning-rate/local/adam', header = FALSE, col.names = c('type','adam'))
rmsprop = read.csv('learning-rate/local/rmsprop', header = FALSE, col.names = c('type','rmsprop'))
momentum = read.csv('learning-rate/local/momentum', header = FALSE, col.names = c('type','momentum'))
adadelta = read.csv('learning-rate/local/adadelta', header = FALSE, col.names = c('type','adadelta'))

groupColors <- c(adam = "#d11141", rmsprop = "#00b159", momentum ="#00aedb", adadelta = "#f37735")

training = data.frame(index = c(1,2,3,4,5,6),
                adam = adam[0:6,2], 
                rmsprop = rmsprop[0:6,2],
                momentum = momentum[0:6,2], 
                adadelta = adadelta[0:6,2])

ml = melt(training, id.vars = c('index') )
plot_training = 
  ggplot(data = ml, aes(x = index, y = value, group = variable)) + 
  geom_line(aes( colour = variable), size = 1.4) + 
  geom_point(aes(color = variable), size = 3.0) + 
  ylab("Logistic Loss") + 
  theme_bw() + 
  scale_color_manual(values = groupColors,  guide = guide_legend(override.aes = list(
    shape = c(NA,NA, NA, NA)))) + 
  scale_x_discrete(name ="Iterations", limits=c("20","40","80","160","320","500")) + 
  theme(legend.text = element_text(size = 20, color = "black"), 
        axis.text=element_text(size=20, color = "black"),
        axis.title=element_text(size=20, color= "black"),
        legend.key.width = unit(3, "cm"), 
        legend.key.height = unit(0.8, "cm"),
        legend.position = "bottom",
        legend.title = element_blank(),
        panel.border = element_rect(colour = "black", fill=NA, size=3)) 

plot <- function(data, c, customize){
  ml = melt(data, id.vars = c('index') )
  maxValue = max(ml$value)
  minValue = max(ml$value) - 0.005
  plot = ggplot(data = ml, aes(x = index, y = value, group = variable)) + 
    geom_line(aes( colour = variable), size = 1.2) + 
    ylab("") +
    xlab("") + 
    theme_bw() + 
    scale_y_continuous(breaks = c(minValue, maxValue), limits=c(minValue, maxValue), labels = c(sprintf("%.3f", minValue),sprintf("%.3f", maxValue))) +
    scale_color_manual(values = c) + 
    theme(axis.text.x=element_blank(),
          axis.text.y=element_text(size=20, color = "black"),
          axis.title=element_text(size=20, color= "black"),  
          legend.position="none",
          panel.border = element_rect(colour = "black", fill=NA, size=3)) 
  
  if (customize == TRUE){
    plot = plot + 
      scale_x_continuous(name ="Time",breaks = c(5,29,53,77,100) ,labels=c("00:00","06:00","12:00","18:00","")) + 
      theme(axis.text.x=element_text(size=20, color = "black"))
  }
  return (plot)
}

data = data.frame(index = 1:101,adam = adam[7:107,2])
plot_adam = plot(data, groupColors['adam'], customize = TRUE)

data = data.frame(index = 1:101,rmsprop = rmsprop[7:107,2])
plot_rmsprop = plot(data,groupColors['rmsprop'],customize = FALSE)

data= data.frame(index = 1:101,momentum = momentum[7:107,2])
plot_momentum = plot(data,groupColors['momentum'],customize = FALSE)

data = data.frame(index = 1:101,adadelta = adadelta[7:107,2])
plot_adadelta = plot(data,groupColors['adadelta'],customize = FALSE)

g_legend<-function(a.gplot){
  tmp <- ggplot_gtable(ggplot_build(a.gplot))
  leg <- which(sapply(tmp$grobs, function(x) x$name) == "guide-box")
  legend <- tmp$grobs[[leg]]
  return(legend)}

mylegend<-g_legend(plot_training)

lay <- rbind(c(1),c(2),c(3),c(4))

finalPlot = grid.arrange(arrangeGrob(
    arrangeGrob(plot_training + theme(legend.position="none") ,top = textGrob("Training Phase", gp=gpar(fontsize=20))), 
    arrangeGrob(plot_adadelta,plot_momentum, plot_rmsprop, plot_adam, layout_matrix = lay, top = textGrob("Deployment Phase", gp=gpar(fontsize=20))), nrow = 1,
  widths = c(7,9)), 
  nrow = 2,
  mylegend, heights=c(15, 1)
)


ggsave(finalPlot , filename = 'learning-rate/local/learning-rate-experiment.eps', 
       device = 'eps', 
       width = 10, height = 8, 
       units = "in")
