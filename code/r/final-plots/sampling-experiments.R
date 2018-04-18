setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/")
library(ggplot2)
library(reshape)
library(tikzDevice)
library(ggpubr)


urlDataProcessing <- function(){
  weighted = cumsum(read.csv('url-reputation/sampling-modes/confusion_matrix-time_based-100', header = FALSE, col.names = c('tp','fp','tn','fn')))
  weighted$mc = (weighted$fp + weighted$fn) / (weighted$fp + weighted$fn + weighted$tp + weighted$tn)
  window = cumsum(read.csv('url-reputation/sampling-modes/confusion_matrix-window(1000)-100', header = FALSE, col.names = c('tp','fp','tn','fn')))
  window$mc = (window$fp + window$fn) / (window$fp + window$fn + window$tp + window$tn)
  uniform = cumsum(read.csv('url-reputation/sampling-modes/confusion_matrix-uniform-100', header = FALSE, col.names = c('tp','fp','tn','fn')))
  uniform$mc = (uniform$fp + uniform$fn) / (uniform$fp + uniform$fn + uniform$tp + uniform$tn)
  
  append <- function(vec, maxLength){
    return (c(vec,rep(NA, maxLength - length(vec))))
  }
  maxLength = nrow(uniform)
  
  df = data.frame(Time = 1:nrow(uniform),
                  Timebased = weighted$mc,
                  Windowbased = window$mc, 
                  Uniform = uniform$mc)
  
  DAY_DURATION = 500
  df = df[((df$Time %% DAY_DURATION == 0) | df$Time == 1), ]
  df$Timebased = df$Timebased * 100
  df$Windowbased = df$Windowbased * 100
  df$Uniform = df$Uniform * 100
  ml = melt(df, id.vars = 'Time', variable_name ='Sampling')
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
                 shape = '-1', linetype ='Sampling', size =2, color = "Sampling", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = urlBreaks, labels= urlLabels)
urlPlot = ggpar(urlPlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize)) +
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.2,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-1)),
        axis.text.x = element_text(margin = margin(t=-1)))

taxiPlot = ggline(taxiData, 'Time', 'value', ylab = "RMSLE", xlab = '(b) Taxi',
                  shape = '-1', linetype ='Sampling',size = 2, color = "Sampling", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = taxiBreaks, labels = taxiLabels)
taxiPlot = ggpar(taxiPlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize))+
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-1)),
        axis.text.x = element_text(margin = margin(t=-1)))

criteoPlot = ggline(criteoData, 'Time', 'value', ylab = "MSE", xlab = '(c) Criteo',
                    shape = '-1', linetype ='Sampling', size =2, color = "Sampling", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = criteoBreaks, labels= criteoLabels)
criteoPlot = ggpar(criteoPlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize)) +
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-1)),
        axis.text.x = element_text(margin = margin(t=-1)))


samplingPlot = ggarrange(urlPlot, taxiPlot,criteoPlot,  nrow = 1, ncol = 3, common.legend = TRUE)
tikz(file = "../images/experiment-results/tikz/sampling-mode-figure.tex", width = 6, height = 2)
samplingPlot
dev.off()
