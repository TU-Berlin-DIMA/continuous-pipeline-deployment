setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/")
library(ggplot2)
library(reshape)
library(tikzDevice)
library(ggpubr)

# misclassiffication
weighted = cumsum(read.csv('sampling/continuous/confusion_matrix-fixed_time_based', header = FALSE, col.names = c('tp','fp','tn','fn')))
weighted$mc = (weighted$fp + weighted$fn) / (weighted$fp + weighted$fn + weighted$tp + weighted$tn)
#online = cumsum(read.csv('sampling/continuous/confusion_matrix-online', header = FALSE, col.names = c('tp','fp','tn','fn')))
#online$mc = (online$fp + online$fn) / (online$fp + online$fn + online$tp + online$tn)
window = cumsum(read.csv('sampling/continuous/confusion_matrix-window(1000)', header = FALSE, col.names = c('tp','fp','tn','fn')))
window$mc = (window$fp + window$fn) / (window$fp + window$fn + window$tp + window$tn)
uniform = cumsum(read.csv('sampling/continuous/confusion_matrix-uniform', header = FALSE, col.names = c('tp','fp','tn','fn')))
uniform$mc = (uniform$fp + uniform$fn) / (uniform$fp + uniform$fn + uniform$tp + uniform$tn)
breaks = c(1,2000,4000,6000, 8000, 10000, 12000)
labels = c("Deploy","20","40", "60", "80", "100", "120")


df = data.frame(Time = 1:nrow(window),
                weighted = weighted$mc,
                # online = online$mc, 
                window = window$mc, 
                uniform = uniform$mc)
DAY_DURATION = 100
df = df[((df$Time %% (10 * DAY_DURATION) == 0) | df$Time == 200 ), ]
df$weighted = df$weighted * 100
df$window = df$window * 100
df$uniform = df$uniform * 100
ml = melt(df, id.vars = 'Time', variable_name ='Sampling')
fontLabelSize = 10
baseSize = 14
url_plot = ggline(ml, 'Time','value', ylab = "Misclassification (\\%)", xlab = 'Time (day)',
                  shape = '-1', linetype ='Sampling', color = "Sampling", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = breaks, labels= labels)
url_plot = ggpar(url_plot, legend = "top", legend.title = "", font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(plot.margin = unit(c(0,0,0,0), "lines"))

criteo_plot = ggline(ml, 'Time','value', ylab = "Logistic Loss", xlab = 'Time (day)',
                     shape = '-1', linetype ='Sampling', color = "Sampling",  ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = breaks, labels= labels) + rremove('legend') 
criteo_plot = ggpar(criteo_plot, font.x = c(fontLabelSize), font.y=c(fontLabelSize) ) + theme(plot.margin = unit(c(0,0,0,0), "lines"))


taxi_plot = ggline(ml, 'Time','value', ylab = "MSE", xlab = 'Time (day)',
                   shape = '-1', linetype ='Sampling', color = "Sampling", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = breaks, labels = labels) + rremove('legend') 
taxi_plot = ggpar(taxi_plot, font.x = c(fontLabelSize), font.y=c(fontLabelSize) ) + theme(plot.margin = unit(c(0,0,0,0), "lines"))


sampling_plot = ggarrange(url_plot,ggarrange(taxi_plot,criteo_plot, nrow = 1, ncol = 2), nrow = 2, ncol = 1) + rremove('legend') 

tikz(file = "../../images/experiment-results/tikz/sampling-experiment.tex", width = 4, height = 4)
sampling_plot 
dev.off()
