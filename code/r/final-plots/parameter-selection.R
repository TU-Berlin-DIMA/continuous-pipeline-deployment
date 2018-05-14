setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/")
library(ggplot2)
library(reshape)
library(tikzDevice)
library(ggpubr)
source('../code/r/final-plots/functions.r')


urlHyperProcessing <- function (){
  hyperParams = read.csv('url-reputation/param-selection/training', header = FALSE, col.names = c('updater','reg','tp','fp','tn','fn'))
  hyperParams$mc = (hyperParams$fp + hyperParams$fn) / (hyperParams$fp + hyperParams$fn + hyperParams$tp + hyperParams$tn)
  results = data.frame(hyperParams[,c("updater","reg","mc")])
}

taxiProcessing <- function (){
  hyperParams = read.csv('nyc-taxi/param-selection-old/training', header = FALSE, col.names = c('updater','reg','ssl','count'))
  hyperParams$rmsle = sqrt(hyperParams$ssl/hyperParams$count)
  results = data.frame(hyperParams[,c("updater","reg","rmsle")])
}

criteoProcessing <- function (){
  hyperParams = read.csv('criteo/param-selection/training', header = FALSE, col.names = c('updater','reg','loss','count'))
  hyperParams$ll = sqrt(hyperParams$loss/hyperParams$count)
  results = data.frame(hyperParams[,c("updater","reg","ll")])
}

urlTable = urlHyperProcessing()
#write.table(urlTable, file = '../images/experiment-results/tikz/ps-url-table.csv')

taxiTable = taxiProcessing()
#write.table(taxiTable, file = '../images/experiment-results/tikz/ps-taxi-table.csv')

criteoTable = criteoProcessing()
#write.table(criteoTable, file = '../images/experiment-results/tikz/ps-criteo-table.csv')

## Streaming data
urlDataProcessing <- function(){
  Adam = getMisclassification('url-reputation/param-selection/adam-0.001/confusion_matrix-time_based-100')
  Rmsprop = getMisclassification('url-reputation/param-selection/rmsprop-0.001/confusion_matrix-time_based-100')
  Adadelta = getMisclassification('url-reputation/param-selection/adadelta-0.001/confusion_matrix-time_based-100')
  
  df = data.frame(Time = 1:length(Adam), Adam, Rmsprop, Adadelta)
  DAY_DURATION = 500
  df = df[((df$Time %% DAY_DURATION == 0) | df$Time == 100), ]
  df[,-1] = df[,-1] * 100
  ml = melt(df, id.vars = 'Time', variable_name ='Adaptation')
  return (ml)
}

criteoDataProcessing <- function(){
  getMisclassification <-function(loc){
    confusionMatrix = cumsum(read.csv(loc, header = FALSE, col.names = c('tp','fp','tn','fn')))
    return((confusionMatrix$fp + confusionMatrix$fn) / (confusionMatrix$fp + confusionMatrix$fn + confusionMatrix$tp + confusionMatrix$tn))
  }
  Adam = getMisclassification('url-reputation/param-selection/adam-0.001/confusion_matrix-time_based-100')
  Rmsprop = getMisclassification('url-reputation/param-selection/rmsprop-0.001/confusion_matrix-time_based-100')
  Adadelta = getMisclassification('url-reputation/param-selection/adadelta-0.001/confusion_matrix-time_based-100')
  
  df = data.frame(Time = 1:length(Adam), Adam, Rmsprop, Adadelta)
  DAY_DURATION = 500
  df = df[((df$Time %% DAY_DURATION == 0) | df$Time == 100), ]
  df[,-1] = df[,-1] * 100
  df[,c(2,3,4)] = 0
  ml = melt(df, id.vars = 'Time', variable_name ='Adaptation')
  return (ml)
}

taxiDataProcessing <- function(){
  Adam = getRMSLE('nyc-taxi/param-selection/adam-1.0E-4/continuous-with-optimization-time_based-720/rmsle')
  Rmsprop = getRMSLE('nyc-taxi/param-selection/rmsprop-1.0E-4/continuous-with-optimization-time_based-720/rmsle')
  Adadelta = getRMSLE('nyc-taxi/param-selection/adadelta-1.0E-4/continuous-with-optimization-time_based-720/rmsle')
  
  df = data.frame(Time = 1:length(Adam), Adam, Rmsprop, Adadelta)
  DAY_DURATION = 250
  df = df[((df$Time %% DAY_DURATION == 0) | df$Time == 50), ]
  ml = melt(df, id.vars = 'Time', variable_name ='Adaptation')
  return (ml)
}

fontLabelSize = 14
baseSize = 20

####### URL PLOT ##########
urlData = urlDataProcessing()
urlBreaks = c(100, 1500,3000)
urlLabels = c("day1","day15","day30")
urlPlot = ggline(urlData, 'Time', 'value', ylab = "Misclassification\\%", xlab = '(a) URL',
                  shape = '-1', linetype ='Adaptation', size =2, color = "Adaptation", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = urlBreaks, labels= urlLabels)
urlPlot = ggpar(urlPlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize)) +
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.2,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-5)),
        axis.text.x = element_text(margin = margin(t=-1)))

####### TAXI PLOT ##########
taxiData = taxiDataProcessing()
taxiBreaks = c(50,2000)
taxiLabels = c("Feb15", "Apr15")
taxiPlot = ggline(taxiData, 'Time', 'value', ylab = "RMSLE", xlab = '(b) Taxi',
                   shape = '-1', linetype ='Adaptation',size = 2, color = "Adaptation", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = taxiBreaks, labels = taxiLabels)
taxiPlot = ggpar(taxiPlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize))+
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-5)),
        axis.text.x = element_text(margin = margin(t=-1)))

####### CRITEO PLOT ##########
criteoData = criteoDataProcessing()
criteoBreaks = c(100,1500,3000)
criteoLabels = c("day1", "day3","day6")
criteoPlot = ggline(criteoData, 'Time', 'value', ylab = "MSE", xlab = '(c) Criteo',
                     shape = '-1', linetype ='Adaptation', size =2, color = "Adaptation", ggtheme = theme_pubclean(base_size = baseSize),
                    ylim=c(min(urlData$value), max(urlData$value))) + 
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
