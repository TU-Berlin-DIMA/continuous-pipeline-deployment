setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/")
library(ggplot2)
library(reshape)
library(tikzDevice)
library(ggpubr)



urlDataProcessing <- function(){
  
  scale = 1000 * 60
  online = colSums(read.csv('url-reputation/deployment-modes-quality-time/confusion_matrix-online', header = FALSE, col.names = c('tp','fp','tn','fn')))
  oMC = (online[[2]] + online[[4]]) / sum(online) * 100
  oTime = read.csv('url-reputation/deployment-modes-quality-time/online-time', header = FALSE, col.names = c('time'))$time / scale
  
  continuous = colSums(read.csv('url-reputation/deployment-modes-quality-time/confusion_matrix-time_based-100-with-optimization', header = FALSE, col.names = c('tp','fp','tn','fn')))
  cMC = (continuous[[2]] + continuous[[4]]) / sum(continuous) * 100
  cTime = read.csv('url-reputation/deployment-modes-quality-time/continuous-full-optimization-time', header = FALSE, col.names = c('time'))$time / scale
  
  periodical = colSums(read.csv('url-reputation/deployment-modes-quality-time/confusion_matrix-periodical-warm', header = FALSE, col.names = c('tp','fp','tn','fn')))
  pMC = (periodical[[2]] + periodical[[4]]) / sum(periodical) * 100
  pTime = read.csv('url-reputation/deployment-modes-quality-time/periodical-warm-time', header = FALSE, col.names = c('time'))$time / scale
  
  
  df = data.frame(Time = c(cTime, pTime,oTime),
                  MC = c(cMC, pMC, oMC),
                  Deployment = factor(c('Continuous', 'Periodical','Online'), levels = c('Continuous', 'Periodical','Online')))
  
  
  return(df)
}

urlData = urlDataProcessing()
criteoData = urlDataProcessing()
taxiData = urlDataProcessing()
fontLabelSize = 14
baseSize = 20
urlBreaks = c(0, 400 ,800)
taxiBreaks = c(0, 400 ,800)
criteoBreaks = c(0, 400 ,800)

urlPlot = ggscatter(urlData, x = "Time", 
          y= "MC", 
          color = "Deployment", 
          shape = "Deployment", size = 2.5, ylim = c(2.14,2.22),
          ylab = 'Misclassification', xlab = "Time(m)\n(a) URL",
          ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = urlBreaks) + 
  theme( legend.title = element_text(size = 0), 
         plot.margin = unit(c(0,1.5,0,0), "lines"), 
         axis.title.y = element_text(margin = margin(r=-1)),
         axis.text.x = element_text(margin = margin(t=-1)))

urlPlot = ggpar(urlPlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize))

taxiPlot = ggscatter(taxiData, x = "Time", 
                    y= "MC", 
                    color = "Deployment", 
                    shape = "Deployment", size = 2.5 ,ylim = c(2.14,2.22),
                    ylab = 'RMSLE', xlab = "Time(m)\n(b) Taxi",
                    ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = taxiBreaks) + 
  theme(legend.title = element_text(size = 0), 
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
         axis.title.y = element_text(margin = margin(r=-1)),
         axis.text.x = element_text(margin = margin(t=-1)))
taxiPlot = ggpar(taxiPlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize))

criteoPlot = ggscatter(criteoData, x = "Time", 
                    y= "MC", 
                    color = "Deployment", 
                    shape = "Deployment", size = 2.5 ,ylim = c(2.14,2.22),
                    ylab = 'MSE', xlab = "Time(m)\n(c) Criteo",
                    ggtheme = theme_pubclean(base_size = baseSize)) +
  scale_x_continuous(breaks = criteoBreaks) +
  theme( legend.title = element_text(size = 0), 
         plot.margin = unit(c(0,1.5,0,0), "lines"), 
         axis.title.y = element_text(margin = margin(r=-1)),
         axis.text.x = element_text(margin = margin(t=-1)))
criteoPlot = ggpar(criteoPlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize))

qualityVsTime = ggarrange(urlPlot, taxiPlot, criteoPlot, nrow = 1, ncol = 3, common.legend = TRUE)

tikz(file = "../images/experiment-results/tikz/quality-vs-time.tex", width = 6, height = 2.2)
qualityVsTime 
dev.off()