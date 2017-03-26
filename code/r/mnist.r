setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/")
library(ggplot2)
library(reshape)

loadData <- function(file){
  library(readr)
  library(stringr)
  data = read_file(file)
  data = str_replace_all(data, " ", "")
  data = strsplit(data, ',')
  return(as.numeric(data[[1]]))
}


loadQuality <- function(file){
  library(readr)
  library(stringr)
  data = read_file(file)
  data = str_replace_all(data, " ", "")
  data = strsplit(data, ']')
  one = strsplit(data[[1]][1], ',')
  two = strsplit(data[[1]][2], ',')
  three = strsplit(data[[1]][3], ',')
  four = strsplit(data[[1]][4], ',')
  five = strsplit(data[[1]][5], ',')
  six = strsplit(data[[1]][6], ',')
  seven = strsplit(data[[1]][7], ',')
  eight = strsplit(data[[1]][8], ',')
  nine = strsplit(data[[1]][9], ',')
  ten = strsplit(data[[1]][10], ',')
  
  return(list(one = as.numeric(one[[1]]), 
              two = as.numeric(two[[1]]),
              three = as.numeric(three[[1]]),
              four = as.numeric(four[[1]]),
              five = as.numeric(five[[1]]),
              six = as.numeric(six[[1]]),
              seven = as.numeric(seven[[1]]),
              eight = as.numeric(eight[[1]]),
              nine = as.numeric(nine[[1]]),
              ten = as.numeric(ten[[1]]))) 
}

# Plot MNIST 


continuous = read.csv(file = 'mnist/nn/500/continuous-error.txt', header = FALSE, col.names = 'continuous')
velox =  read.csv('mnist/nn/500/velox-error.txt',  header = FALSE, col.names = 'velox')
baselinePlus =  read.csv('mnist/nn/500/offline-online.txt',  header = FALSE, col.names = 'baselinePlus')
baseline=  read.csv('mnist/nn/500/offline-only.txt',  header = FALSE, col.names = 'baseline')

m = max(nrow(continuous), nrow(velox), nrow(baseline), nrow(baselinePlus))
continuous = rbind(continuous, data.frame(continuous = rep(tail(continuous[[1]], 1), m - nrow(continuous))))
velox = rbind(velox, data.frame(velox = rep(tail(velox[[1]], 1), m - nrow(velox))))
baseline = rbind(baseline, data.frame(baseline = rep(tail(baseline[[1]], 1), m - nrow(baseline))))
baselinePlus = rbind(baselinePlus, data.frame(baselinePlus = rep(tail(baselinePlus[[1]], 1), m - nrow(baselinePlus))))

df = data.frame(time = 1:nrow(continuous),
                continuous = continuous, 
                velox = velox,
                baseline = baseline,
                baselinePlus = baselinePlus)

retrainings = c(83,169,248,335,420)

p = 
  ggplot(data = df) + 
  # plot lines
  geom_line(aes(x = time, y  = baseline, linetype = "a", color = "a"), size = 1, linetype = "dotted") + 
  geom_line(aes(x = time, y  = baselinePlus, linetype = "b", color = "b"), linetype = "dotdash", size = 1) + 
  geom_line(aes(x = time, y  = continuous, linetype = "c", color = "c"), linetype = "solid", size = 1) + 
  geom_line(aes(x = time, y  = velox, linetype = "d", color = "d"), linetype = "longdash", size = 1) + 
  # plot retraining points
  geom_point(data = df[retrainings,c(1,3)], aes(x=time, y = velox, shape = "e", color = "e"), lwd = 4, shape = 17 ) + 
  # x and y labels
  xlab("Time") + ylab("Misclassification Rate") + 
  #ylim(c(0.1,1.5)) + 
  # legend themes
  theme_bw() + 
  theme(legend.text = element_text(size = 20, color = "black"), 
        legend.key = element_rect(colour = "transparent", fill = "transparent"), 
        legend.background = element_rect(colour = "transparent", fill = "transparent"), 
        axis.text=element_text(size=28, color = "black"),
        axis.title=element_text(size=28, color= "black"),  
        legend.position=c(0.85,0.7), 
        legend.key.width = unit(2.5, "cm"), 
        legend.key.height = unit(0.8, "cm")) + 
  scale_linetype_discrete(guide=FALSE) + 
  scale_shape_discrete(guide=FALSE) + 
  scale_color_manual(name = "", 
                     labels = c("baseline", "baseline+", "continuous","velox", "retraining"),
                     values = c("a"="black", "b"="black","c"="black","d"="black", "e"="black"))+
  guides(color=guide_legend(override.aes=list(shape=c(NA,NA,NA,NA,17),linetype=c(3,4,1,5,0)))) 

ggsave(p , filename = 'mnist/nn/500/mnist-quality.eps', 
       device = 'eps', dpi = 1000,
       width = 14, height = 5, 
       units = "in")


# Plot quality vs buffer size
data = loadQuality('mnist/nn/buffer-size/buffer-size.txt')
df = data.frame(ind = 1:501, b5000 = data$ten , b2500 = data$five,  b500 = data$one)

ml = melt(df, id.vars = 'ind')
samplingRatePlot = 
  ggplot(data = ml, aes(x = ind, y = value, group = variable)) + 
  geom_line(aes( linetype = variable), size = 1.0) + 
  xlab("Testing Increments") + ylab("Error Rate")  + 
  scale_linetype_manual("Buffer Size", labels = c("5000", "2500", "500") , values = c("b5000"=3, "b2500"=5, "b500"=1)) + 
  theme_bw() + 
  theme(legend.title = element_text(size = 30, color = "black"),
        legend.text = element_text(size = 30, color = "black"), 
        legend.key = element_rect(colour = "transparent", fill = "transparent"), 
        legend.key.width = unit(2.5, "cm"), 
        legend.key.height = unit(1.0, "cm"),
        legend.background = element_rect(colour = "transparent", fill = "transparent"), 
        axis.text=element_text(size=30, color = "black"),
        axis.title=element_text(size=32, color = "black"),  
        legend.position=c(0.85,0.7))


ggsave(samplingRatePlot , filename = 'mnist/nn/buffer-size/mnist-buffersize-improved.eps', 
       device = cairo_ps, 
       width = 14, height = 5, 
       units = "in")



# Plot quality vs Sampling rate
data = loadQuality('mnist/nn/sampling/samples.txt')
df = data.frame(ind = 1:501 ,  s0.1 = data$one, s0.5 = data$five, s1.0 = data$ten)

ml = melt(df, id.vars = 'ind')
samplingRatePlot = 
  ggplot(data = ml, aes(x = ind, y = value, group = variable)) + 
  geom_line(aes( linetype = variable), size = 1) + 
  xlab("Testing Increments") + ylab("Error Rate")  + 
  scale_linetype_manual("Sampling Rate", labels = c("0.1", "0.5", "1.0") , values = c("s0.1"=3, "s0.5"=5, "s1.0"=1)) + 
  theme_bw() + 
  theme(legend.title = element_text(size = 30, color = "black"),
        legend.text = element_text(size = 30, color = "black"), 
        legend.key = element_rect(colour = "transparent", fill = "transparent"), 
        legend.key.width = unit(2.5, "cm"), 
        legend.key.height = unit(1.0, "cm"),
        legend.background = element_rect(colour = "transparent", fill = "transparent"), 
        axis.text=element_text(size=30, color = "black"),
        axis.title=element_text(size=32, color = "black"),  
        legend.position=c(0.85,0.7))

ggsave(samplingRatePlot , filename = 'mnist/nn/sampling/mnist-sampling-improved.eps', 
       device = cairo_ps,
       width = 14, height = 5, 
       units = "in")
