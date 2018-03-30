setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/")
library(ggplot2)
library(reshape)
library(tikzDevice)
library(ggpubr)


## Read data for the batch training
hyperParams = read.csv('param-selection/training', header = FALSE, col.names = c('updater','reg','tp','fp','tn','fn','iter'))
hyperParams$mc = (hyperParams$fp + hyperParams$fn) / (hyperParams$fp + hyperParams$fn + hyperParams$tp + hyperParams$tn)
results = data.frame(hyperParams[,c("updater","reg","mc","iter")])
#write.table(results, file = '../../images/experiment-results/tikz/ps-table.csv')

## Streaming data
urlDataProcessing <- function(){
  adam = cumsum(read.csv('param-selection/adam/confusion_matrix-time_based-100', header = FALSE, col.names = c('tp','fp','tn','fn')))
  adam$mc = (adam$fp + adam$fn) / (adam$fp + adam$fn + adam$tp + adam$tn)
  rmsprop = cumsum(read.csv('param-selection/rmsprop/confusion_matrix-time_based-100', header = FALSE, col.names = c('tp','fp','tn','fn')))
  rmsprop$mc = (rmsprop$fp + rmsprop$fn) / (rmsprop$fp + rmsprop$fn + rmsprop$tp + rmsprop$tn)
  adadelta = cumsum(read.csv('param-selection/adadelta/confusion_matrix-time_based-100', header = FALSE, col.names = c('tp','fp','tn','fn')))
  adadelta$mc = (adadelta$fp + adadelta$fn) / (adadelta$fp + adadelta$fn + adadelta$tp + adadelta$tn)
  momentum = cumsum(read.csv('param-selection/momentum/confusion_matrix-time_based-100', header = FALSE, col.names = c('tp','fp','tn','fn')))
  momentum$mc = (momentum$fp + momentum$fn) / (momentum$fp + momentum$fn + momentum$tp + momentum$tn)
  append <- function(vec, maxLength){
    return (c(vec,rep(NA, maxLength - length(vec))))
  }
  maxLength = nrow(adam)
  df = data.frame(Time = 1:nrow(adam), adam = adam$mc, rmsprop = rmsprop$mc, adadelta = adadelta$mc, momentum = momentum$mc)
  DAY_DURATION = 100
  df = df[((df$Time %% DAY_DURATION == 0) | df$Time == 200), ]
  df$adam = df$adam * 100
  df$rmsprop = df$rmsprop * 100
  df$adadelta = df$adadelta * 100
  df$momentum = df$momentum * 100
  ml = melt(df, id.vars = 'Time', variable_name ='Adaptation')
  return (ml)
}

criteoDataProcessing <- function(){
  
}

taxiDataProcessing <- function(){
  
}


urlData = urlDataProcessing()
criteoData = urlDataProcessing()
taxiData = urlDataProcessing()

fontLabelSize = 14
baseSize = 20
breaks = c(1,200,400,600, 800, 1000)
labels = c("0","2","4", "6", "8", "10")

url_plot = ggline(urlData, 'Time', 'value', ylab = "Misclass (\\%)", xlab = 'Day',
                  shape = '-1', linetype ='Adaptation', size =2, color = "Adaptation", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = breaks, labels= labels)
url_plot = ggpar(url_plot, font.x = c(fontLabelSize), font.y=c(fontLabelSize), legend.title = NULL) +
  theme(plot.margin = unit(c(0,0,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-3)),
        axis.title.x = element_text(margin = margin(t=-2)),
        axis.text.x = element_text(margin = margin(t=-3)))

criteo_plot = ggline(criteoData, 'Time', 'value', ylab = "MSE", xlab = 'Day',
                     shape = '-1', linetype ='Adaptation', size =2, color = "Adaptation", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = breaks, labels= labels)
criteo_plot = ggpar(criteo_plot, font.x = c(fontLabelSize), font.y=c(fontLabelSize), legend.title = NULL) +
  theme(plot.margin = unit(c(0,0,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-3)),
        axis.title.x = element_text(margin = margin(t=-2)),
        axis.text.x = element_text(margin = margin(t=-3)))

taxi_plot = ggline(taxiData, 'Time', 'value', ylab = "MSE", xlab = 'Day',
                   shape = '-1', linetype ='Adaptation',size = 2, color = "Adaptation", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = breaks, labels = labels)
taxi_plot = ggpar(taxi_plot, font.x = c(fontLabelSize), font.y=c(fontLabelSize), legend.title = NULL)+
  theme(plot.margin = unit(c(0,0,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-3)),
        axis.title.x = element_text(margin = margin(t=-2)),
        axis.text.x = element_text(margin = margin(t=-3)))

param_selection_plot = ggarrange(url_plot, taxi_plot,criteo_plot,  nrow = 1, ncol = 3, common.legend = TRUE) + theme(legend.title = NULL)
tikz(file = "../../images/experiment-results/tikz/parameter-selection-figure.tex", width = 6, height = 2)
param_selection_plot
dev.off()
