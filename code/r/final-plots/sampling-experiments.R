setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/")
library(ggplot2)
library(reshape)
library(tikzDevice)
library(ggpubr)


urlDataProcessing <- function(){
  weighted = cumsum(read.csv('sampling/continuous/confusion_matrix-time_based-100', header = FALSE, col.names = c('tp','fp','tn','fn')))
  weighted$mc = (weighted$fp + weighted$fn) / (weighted$fp + weighted$fn + weighted$tp + weighted$tn)
  window = cumsum(read.csv('sampling/continuous/confusion_matrix-window(1000)-100', header = FALSE, col.names = c('tp','fp','tn','fn')))
  window$mc = (window$fp + window$fn) / (window$fp + window$fn + window$tp + window$tn)
  uniform = cumsum(read.csv('sampling/continuous/confusion_matrix-uniform-100', header = FALSE, col.names = c('tp','fp','tn','fn')))
  uniform$mc = (uniform$fp + uniform$fn) / (uniform$fp + uniform$fn + uniform$tp + uniform$tn)
  
  df = data.frame(Time = 1:nrow(window),
                  weighted = weighted$mc,
                  window = window$mc, 
                  uniform = uniform$mc)
  
  DAY_DURATION = 100
  df = df[((df$Time %% DAY_DURATION == 0) | df$Time == 200), ]
  df$weighted = df$weighted * 100
  df$window = df$window * 100
  df$uniform = df$uniform * 100
  ml = melt(df, id.vars = 'Time', variable_name ='Sampling')
  return (ml)
}

criteoDataProcessing <- function(){
  
}

taxiDataProcessing <- function(){
  
}

urlData = urlDataProcessing()
criteoData = urlDataProcessing()
taxiData = urlDataProcessing()

fontLabelSize = 10
baseSize = 14
breaks = c(1,2000,4000,6000, 8000, 10000, 12000)
labels = c("Deploy","20","40", "60", "80", "100", "120")
url_plot = ggline(urlData, 'Time', 'value', ylab = "Misclassification (\\%)", xlab = 'Time (day)',
                   shape = '-1', linetype ='Sampling', color = "Sampling", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = breaks, labels= labels)
url_plot = ggpar(url_plot, legend = "top", legend.title = "", font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(plot.margin = unit(c(0,0,0,0), "lines"))

criteo_plot = ggline(criteoData, 'Time', 'value', ylab = "Logistic Loss", xlab = 'Time (day)',
                     shape = '-1', linetype ='Sampling', color = "Sampling", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = breaks, labels= labels) + rremove('legend') 
criteo_plot = ggpar(criteo_plot, font.x = c(fontLabelSize), font.y=c(fontLabelSize) ) + theme(plot.margin = unit(c(0,0,0,0), "lines"))


taxi_plot = ggline(taxiData, 'Time','value', ylab = "MSE", xlab = 'Time (day)',
                   shape = '-1', linetype ='Sampling', color = "Sampling", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = breaks, labels = labels) + rremove('legend') 
taxi_plot = ggpar(taxi_plot, font.x = c(fontLabelSize), font.y=c(fontLabelSize) ) + theme(plot.margin = unit(c(0,0,0,0), "lines"))


sampling_plot = ggarrange(url_plot,ggarrange(taxi_plot,criteo_plot, nrow = 1, ncol = 2), nrow = 2, ncol = 1) + rremove('legend') 

tikz(file = "../../images/experiment-results/tikz/sampling-experiment.tex", width = 4, height = 4)
sampling_plot 
dev.off()

