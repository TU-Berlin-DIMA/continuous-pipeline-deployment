setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/")
library(ggplot2)
library(reshape)
library(tikzDevice)
library(ggpubr)
source('../code/r/final-plots/functions.r')


urlHyperProcessing <- function (){
  hyperParams = read.csv('url-reputation/final/param-selection/training', header = FALSE, col.names = c('updater','reg','tp','fp','tn','fn'))
  hyperParams$mc = (hyperParams$fp + hyperParams$fn) / (hyperParams$fp + hyperParams$fn + hyperParams$tp + hyperParams$tn)
  results = data.frame(hyperParams[,c("updater","reg","mc")])
}

taxiProcessing <- function (){
  hyperParams = read.csv('nyc-taxi/final/param-selection/training', header = FALSE, col.names = c('updater','reg','ssl','count'))
  hyperParams$rmsle = sqrt(hyperParams$ssl/hyperParams$count)
  results = data.frame(hyperParams[,c("updater","reg","rmsle")])
}

criteoProcessing <- function (){
  hyperParams = read.csv('criteo/final/param-selection/training', header = FALSE, col.names = c('updater','reg','loss','count'))
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
  Adam = getMisclassification('url-reputation/final/param-selection/adam-0.001/continuous-with-optimization-time_based-100/confusion_matrix')
  Rmsprop = getMisclassification('url-reputation/final/param-selection/rmsprop-0.001/continuous-with-optimization-time_based-100/confusion_matrix')
  Adadelta = getMisclassification('url-reputation/final/param-selection/adadelta-0.001/continuous-with-optimization-time_based-100/confusion_matrix')
  
  df = data.frame(Time = 1:length(Adam), Adam, Rmsprop, Adadelta)
  DAY_DURATION = 500
  df = df[((df$Time %% DAY_DURATION == 0) | df$Time == 200), ]
  df[,-1] = df[,-1] * 100
  ml = melt(df, id.vars = 'Time', variable_name ='Adaptation')
  return (ml)
}

criteoDataProcessing <- function(){
  Adam = getLogarithmicLoss('criteo/final/param-selection/adam-1.0E-4/continuous-with-optimization-time_based-100/logistic_loss')
  Rmsprop = getLogarithmicLoss('criteo/final/param-selection/rmsprop-1.0E-4/continuous-with-optimization-time_based-100/logistic_loss')
  Adadelta = getLogarithmicLoss('criteo/final/param-selection/adadelta-1.0E-4/continuous-with-optimization-time_based-100/logistic_loss')
  
  
  df = data.frame(Time = 1:length(Adam), Adam, Rmsprop, Adadelta)
  DAY_DURATION = 40
  df = df[((df$Time %% DAY_DURATION == 0) | df$Time == 1), ]
  ml = melt(df, id.vars = 'Time', variable_name ='Adaptation')
  return (ml)
}

taxiDataProcessing <- function(){
  Adam = getRMSLE('nyc-taxi/final/param-selection/adam-1.0E-4/continuous-with-optimization-time_based-720/rmsle')
  Rmsprop = getRMSLE('nyc-taxi/final/param-selection/rmsprop-1.0E-4/continuous-with-optimization-time_based-720/rmsle')
  Adadelta = getRMSLE('nyc-taxi/final/param-selection/adadelta-0.01/continuous-with-optimization-time_based-720/rmsle')
  
  df = data.frame(Time = 1:length(Adam), Adam, Rmsprop, Adadelta)
  DAY_DURATION = 250
  df = df[((df$Time %% DAY_DURATION == 0) | df$Time == 1), ]
  ml = melt(df, id.vars = 'Time', variable_name ='Adaptation')
  return (ml)
}

fontLabelSize = 14
baseSize = 20

####### URL PLOT ##########
urlData = urlDataProcessing()
urlBreaks = c(200, 1500,3000)
urlLabels = c("day1","day15","day30")
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

####### TAXI PLOT ##########
taxiData = taxiDataProcessing()
taxiBreaks = c(1,2000)
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
criteoBreaks = c(1, 376)
criteoLabels = c("day1", "day2")
criteoPlot = ggline(criteoData, 'Time', 'value', ylab = "LogLoss", xlab = '(c) Criteo',
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
