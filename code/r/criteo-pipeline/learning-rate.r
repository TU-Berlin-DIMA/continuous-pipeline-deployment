setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/criteo-full/")
library(ggplot2)
library(reshape)

library(gridExtra)
library(grid)

iterations = c(20,40,80,160,320, 500)

# local
adam = read.csv('learning-rate/local/adam', header = FALSE, col.names = c('adam'))
rmsprop = read.csv('learning-rate/local/rmsprop', header = FALSE, col.names = c('rmsprop'))
#momentum = read.csv('learning-rate/local/momentum', header = FALSE, col.names = c('momentum'))
adadelta = read.csv('learning-rate/local/adadelta', header = FALSE, col.names = c('adadelta'))

#cluster
adam = read.csv('learning-rate/cluster/adam/loss', header = FALSE, col.names = c('iter','adam'))
rmsprop = read.csv('learning-rate/cluster/rmsprop/loss', header = FALSE, col.names = c('iter','rmsprop'))
#momentum = read.csv('learning-rate/cluster/momentum/loss', header = FALSE, col.names = c('iter','momentum'))
adadelta = read.csv('learning-rate/cluster/adadelta/loss', header = FALSE, col.names = c('iter','adadelta'))

groupColors <- c(adam = "#d11141", rmsprop = "#00b159", adadelta = "#f37735")
iters = adam$iter
training = data.frame(index = c(1,2,3,4,5,6),
                      adam = adam$adam,
                      rmsprop = rmsprop$rmsprop,
                      #momentum = momentum$momentum,
                      adadelta = adadelta$adadelta)

ml = melt(training, id.vars = c('index') )
plot_training = 
  ggplot(data = ml, aes(x = index, y = value, group = variable)) + 
  geom_line(aes( colour = variable), size = 1.4) + 
  geom_point(aes(color = variable), size = 3.0) + 
  ylab("Logistic Loss") + 
  theme_bw() + 
  scale_color_manual(values = groupColors,  guide = guide_legend(override.aes = list(shape = c(NA,NA, NA)))) + 
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
  minValue = max(ml$value) - 0.1
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
      scale_x_continuous(name ="Time",breaks = c(5,40,80,120,144) ,labels=c("00:00","06:00","12:00","18:00","")) + 
      theme(axis.text.x=element_text(size=20, color = "black"))
  }
  return (plot)
}

adam = read.csv('learning-rate/cluster/adam/loss_1440', header = FALSE, col.names = c('adam'))
rmsprop = read.csv('learning-rate/cluster/rmsprop/loss_1440', header = FALSE, col.names = c('rmsprop'))
#momentum = read.csv('learning-rate/cluster/momentum/loss', header = FALSE, col.names = c('momentum'))
adadelta = read.csv('learning-rate/cluster/adadelta/loss_1440', header = FALSE, col.names = c('adadelta'))

data = data.frame(index = 1:nrow(adam),adam = adam)
plot_adam = plot(data, groupColors['adam'], customize = TRUE)

data = data.frame(index = 1:nrow(rmsprop),rmsprop = rmsprop)
plot_rmsprop = plot(data,groupColors['rmsprop'],customize = FALSE)

#data= data.frame(index = 1:101,momentum = momentum[7:107,2])
#plot_momentum = plot(data,groupColors['momentum'],customize = FALSE)

data = data.frame(index = 1:nrow(adadelta),adadelta = adadelta)
plot_adadelta = plot(data,groupColors['adadelta'],customize = FALSE)

g_legend<-function(a.gplot){
  tmp <- ggplot_gtable(ggplot_build(a.gplot))
  leg <- which(sapply(tmp$grobs, function(x) x$name) == "guide-box")
  legend <- tmp$grobs[[leg]]
  return(legend)}

mylegend<-g_legend(plot_training)

lay <- rbind(c(1),c(2), c(3))

finalPlot = grid.arrange(arrangeGrob(
    arrangeGrob(plot_training + theme(legend.position="none") ,top = textGrob("Training Phase", gp=gpar(fontsize=20))), 
    arrangeGrob( plot_adadelta,plot_rmsprop, plot_adam, layout_matrix = lay, top = textGrob("Deployment Phase", gp=gpar(fontsize=20))), nrow = 1,
  widths = c(7,9)), 
  nrow = 2,
  mylegend, heights=c(15, 1)
)


ggsave(finalPlot , filename = 'learning-rate/cluster/criteo-learning-rate-experiment.eps', 
       device = 'eps', 
       width = 10, height = 8, 
       units = "in")
