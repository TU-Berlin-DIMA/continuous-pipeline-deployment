setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/")
library(ggplot2)
library(reshape)
library(tikzDevice)
library(ggpubr)


## Read data for the batch training
#hyperParams = read.csv('url-reputation/param-selection/training', header = FALSE, col.names = c('updater','reg','tp','fp','tn','fn','iter'))
#hyperParams$mc = (hyperParams$fp + hyperParams$fn) / (hyperParams$fp + hyperParams$fn + hyperParams$tp + hyperParams$tn)
#results = data.frame(hyperParams[,c("updater","reg","mc","iter")])
#write.table(results, file = '../../images/experiment-results/tikz/ps-table.csv')

## Streaming data
urlDataProcessing <- function(){
  adam = cumsum(read.csv('url-reputation/param-selection/adam/confusion_matrix-time_based-100', header = FALSE, col.names = c('tp','fp','tn','fn')))
  adam$mc = (adam$fp + adam$fn) / (adam$fp + adam$fn + adam$tp + adam$tn)
  rmsprop = cumsum(read.csv('url-reputation/param-selection/rmsprop/confusion_matrix-time_based-100', header = FALSE, col.names = c('tp','fp','tn','fn')))
  rmsprop$mc = (rmsprop$fp + rmsprop$fn) / (rmsprop$fp + rmsprop$fn + rmsprop$tp + rmsprop$tn)
  adadelta = cumsum(read.csv('url-reputation/param-selection/adadelta/confusion_matrix-time_based-100', header = FALSE, col.names = c('tp','fp','tn','fn')))
  adadelta$mc = (adadelta$fp + adadelta$fn) / (adadelta$fp + adadelta$fn + adadelta$tp + adadelta$tn)
  momentum = cumsum(read.csv('url-reputation/param-selection/momentum/confusion_matrix-time_based-100', header = FALSE, col.names = c('tp','fp','tn','fn')))
  momentum$mc = (momentum$fp + momentum$fn) / (momentum$fp + momentum$fn + momentum$tp + momentum$tn)
  append <- function(vec, maxLength){
    return (c(vec,rep(NA, maxLength - length(vec))))
  }
  maxLength = nrow(adam)
  df = data.frame(Time = 1:nrow(adam), Adam = adam$mc, Rmsprop = rmsprop$mc, Adadelta = adadelta$mc, Momentum = momentum$mc)
  DAY_DURATION = 100
  df = df[((df$Time %% DAY_DURATION == 0) | df$Time == 200), ]
  df$Adam = df$Adam * 100
  df$Rmsprop = df$Rmsprop * 100
  df$Adadelta = df$Adadelta * 100
  df$Momentum = df$Momentum * 100
  ml = melt(df, id.vars = 'Time', variable_name ='Adaptation')
  return (ml)
}

criteoDataProcessing <- function(){
  
}

taxiDataProcessing <- function(){
  
}


urlData = urlDataProcessing()
urlBreaks = c(100, 1000)
urlLabels = c("day1","day10")

taxiData = urlDataProcessing()
taxiBreaks = c(100,1000)
taxiLabels = c("Feb15", "Apr15")

criteoData = urlDataProcessing()
criteoBreaks = c(100,1000)
criteoLabels = c("day1", "day5")

fontLabelSize = 14
baseSize = 20


urlPlot = ggline(urlData, 'Time', 'value', ylab = "Misclassification\\%", xlab = '(a) URL',
                  shape = '-1', linetype ='Adaptation', size =2, color = "Adaptation", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = urlBreaks, labels= urlLabels)
urlPlot = ggpar(urlPlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize)) +
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.2,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-1)),
        axis.text.x = element_text(margin = margin(t=-1)))

taxiPlot = ggline(taxiData, 'Time', 'value', ylab = "RMSLE", xlab = '(b) Taxi',
                   shape = '-1', linetype ='Adaptation',size = 2, color = "Adaptation", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = taxiBreaks, labels = taxiLabels)
taxiPlot = ggpar(taxiPlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize))+
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-1)),
        axis.text.x = element_text(margin = margin(t=-1)))

criteoPlot = ggline(criteoData, 'Time', 'value', ylab = "MSE", xlab = '(c) Criteo',
                     shape = '-1', linetype ='Adaptation', size =2, color = "Adaptation", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = criteoBreaks, labels= criteoLabels)
criteoPlot = ggpar(criteoPlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize)) +
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-1)),
        axis.text.x = element_text(margin = margin(t=-1)))

param_selection_plot = ggarrange(urlPlot, taxiPlot, criteoPlot,  nrow = 1, ncol = 3, common.legend = TRUE)
tikz(file = "../images/experiment-results/tikz/parameter-selection-figure.tex", width = 6, height = 2)
param_selection_plot
dev.off()
