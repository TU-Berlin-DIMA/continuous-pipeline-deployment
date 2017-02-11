setwd("~/Documents/work/phd-papers/continuous-training/code/python/continuous-training/results")
library(ggplot2)

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
  data = strsplit(data, '\\[')
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


# Plot Movie lens 100 K data
continuous = loadData('movie-lens-100k/5000/continuous-error.txt')
velox = loadData('movie-lens-100k/5000/velox-error.txt')
baselinePlus = loadData('movie-lens-100k/5000/offline-online.txt')
baseline= loadData('movie-lens-100k/5000/offline-only.txt')


df = data.frame(time = 1:length(continuous),
                continuous = continuous, 
                velox = velox,
                baseline = baseline,
                baselinePlus = baselinePlus)

retrainings = c(830,1699,2485,3355,4204,5000)

p = 
  # data frame
  ggplot(data = df) + 
  # plot lines
  geom_line(aes(x = time, y  = baseline, colour = "a"), size = 1.5) + 
  geom_line(aes(x = time, y  = baselinePlus, colour = "b"), size = 1.5) + 
  geom_line(aes(x = time, y  = continuous, colour = "c"), size = 1.5) + 
  geom_line(aes(x = time, y  = velox, colour = "d"), size = 1.5) + 
  # plot retraining points
  geom_point(data = df[retrainings,c(1,3)], 
             aes(x=time, y = velox, colour="e", fill = "Retraining"), 
             shape = 8, 
             lwd = 7 ) + 
  # x and y labels
  xlab("Test Cycle") + ylab("Mean Squared Error") + 
  # legend themes
  theme(legend.text = element_text(size = 24), legend.key = element_rect(colour = "transparent", fill = alpha('white', 0.0)) ,
        legend.position="bottom") +
  theme(axis.text=element_text(size=20),
        axis.title=element_text(size=28)) + 
  # legend for line graph   
  scale_color_manual(name ="",  # Name,
                     labels = c("Baseline   ", "Baseline+    ", "Continuous    ", "Velox    ", ""), 
                     values = c("a" = "green", "b" = "orange", "c" = "blue","d" = "red", "e" = "black"))  +
  # legend for retraining point
  scale_fill_manual(name = "", values = c("Retraining" = "black")) + 
  # guides for enhancing legend
  guides(color=guide_legend(override.aes=list(shape=c(NA,NA,NA,NA,NA),linetype=c(1,1,1,1,0)))) 


ggsave(p , filename = 'movie-lens-100k/5000/movie-lens-100k-quality-improved.eps', device = 'eps', dpi = 1000)


# Plot Movie lens 1M data
continuous = loadData('movie-lens-1M/50000/continuous-error.txt')
velox = loadData('movie-lens-1M/50000/velox-error.txt')
baselinePlus = loadData('movie-lens-1M/50000/offline-online.txt')
baseline= loadData('movie-lens-1M/50000/offline-only.txt')


df = data.frame(time = 1:length(continuous),
                continuous = continuous, 
                velox = velox,
                baseline = baseline,
                baselinePlus = baselinePlus)

retrainings = c(8379,16706,25075,33345,41765,50000)

library(ggplot2)

p = 
  # data frame
  ggplot(data = df) + 
  # plot lines
  geom_line(aes(x = time, y  = baseline, colour = "a"), size = 1.5) + 
  geom_line(aes(x = time, y  = baselinePlus, colour = "b"), size = 1.5) + 
  geom_line(aes(x = time, y  = continuous, colour = "c"), size = 1.5) + 
  geom_line(aes(x = time, y  = velox, colour = "d"), size = 1.5) + 
  # plot retraining points
  geom_point(data = df[retrainings,c(1,3)], 
             aes(x=time, y = velox, colour="e", fill = "Retraining"), 
             shape = 8, 
             lwd = 7 ) + 
  # x and y labels
  xlab("Test Cycle") + ylab("Mean Squared Error") + 
  # legend themes
  theme(legend.text = element_text(size = 24), legend.key = element_rect(colour = "transparent", fill = alpha('white', 0.0)) ,
        legend.position="bottom") +
  theme(axis.text=element_text(size=20),
        axis.title=element_text(size=28)) + 
  # legend for line graph   
  scale_color_manual(name ="",  # Name,
                     labels = c("Baseline   ", "Baseline+    ", "Continuous    ", "Velox    ", ""), 
                     values = c("a" = "green", "b" = "orange", "c" = "blue","d" = "red", "e" = "black"))  +
  # legend for retraining point
  scale_fill_manual(name = "", values = c("Retraining" = "black")) + 
  # guides for enhancing legend
  guides(color=guide_legend(override.aes=list(shape=c(NA,NA,NA,NA,NA),linetype=c(1,1,1,1,0)))) 


ggsave(p , filename = 'movie-lens-1M/50000/movie-lens-1m-quality-improved.eps', device = 'eps', dpi = 1000)



# Plot buffer size vs time
times = loadData('movie-lens-100k/buffer-size/time.txt')
buffer = seq(500, 5000, 500)

df = data.frame(buffer, times = times/60)

bufferVsTimePlot = ggplot(data = df) + 
  aes(x = buffer, y = times, colour = "blue") + 
  geom_line(size = 1.5) + 
  geom_point(lwd = 4)  + 
  xlab("Buffer size") + ylab("Time (m)") + 
  scale_color_manual(values=c("blue" = "blue")) + 
  theme(axis.text=element_text(size=20),
        axis.title=element_text(size=28)) + 
  guides(colour=FALSE)

ggsave(bufferVsTimePlot , filename = 'movie-lens-100k/buffer-size/movie-lens-100k-buffer-time-improved.eps', device = 'eps', dpi = 1000)

# Plot sampling rate vs time
times = loadData('movie-lens-100k/sampling/times.txt')
buffer = seq(0.1, 1.0, 0.1)

df = data.frame(buffer, times = times/60)

bufferVsTimePlot = ggplot(data = df) + 
  aes(x = buffer, y = times, colour = "blue") + 
  geom_line(size = 1.5) + 
  geom_point(lwd = 4)  + 
  xlab("Sampling Rate") + ylab("Time (m)") + 
  scale_color_manual(values=c("blue" = "blue")) + 
  theme(axis.text=element_text(size=20),
        axis.title=element_text(size=28)) + 
  guides(colour=FALSE)

ggsave(bufferVsTimePlot , filename = 'movie-lens-100k/sampling/movie-lens-100k-sampling-time-improved.eps', device = 'eps', dpi = 1000)

# Plot running time of different work loads
time_100k = c(128.7379, 506.1425, 854.3796, 187.6295)
time_1M = c(235.8159, 4608.0255, 9610.6307, 1825.9909)
# time_MNIST = c(2.3790788650512695, 8.481926918029785, 244.62878799438477, 1569.8254868984222, 56.363343954086304)
models = c('Baseline', 'Continuous', 'Velox', 'Static Training')

df = data.frame("models" = models, "movie_lens_100k"=time_100k, "movie_lens_1M"=time_1M)
melted = melt(df, id.vars = 'models')
colnames(melted) = c("models", "data_sets", "value")

runningTimePlot = ggplot(melted, aes(models, value)) +   
  geom_bar(aes(fill = data_sets), position = "dodge", stat="identity") + 
  xlab("") + ylab("Time (s) in Log Scale") + 
  scale_y_log10() +
  scale_fill_discrete(name = "", labels = c("movie_lens_100k   ", "movie_lens_1M   ")) + 
  theme(legend.position = "bottom" , 
        legend.text = element_text(size = 24),
        axis.text=element_text(size=20),
        axis.title=element_text(size=28)) 

ggsave(runningTimePlot , filename = 'times-log-scale-improved.eps', device = 'eps', dpi = 1000)

# Plot quality vs buffer size
data = loadQuality('movie-lens-100k/buffer-size/buffers.txt')
df = data.frame(ind = 1:5001, one = data$one , two = data$two, 
                three = data$three , four = data$four, 
                five = data$five , six = data$six, 
                seven = data$seven , eight = data$eight, 
                nine = data$nine , ten = data$ten)

ggplot(data = df, aes(x = ind)) +
  geom_ribbon(aes(ymin = one, ymax = two, fill = "500"),  alpha='0.5') + 
  geom_ribbon(aes(ymin = two, ymax = three, fill =  "b" ), alpha='0.5') + 
  geom_ribbon(aes(ymin = three, ymax = four , fill = "c"), alpha='0.5') + 
  geom_ribbon(aes(ymin = four, ymax = five, fill ="d"), alpha='0.5' )+ 
  geom_ribbon(aes(ymin = five, ymax = six , fill = "e"), alpha='0.5') + 
  geom_ribbon(aes(ymin = six, ymax = seven, fill = "f"), alpha='0.5') + 
  geom_ribbon(aes(ymin = seven, ymax = eight , fill = "g"), alpha='0.5') + 
  geom_ribbon(aes(ymin = eight, ymax = nine, fill ="h"), alpha='0.5') + 
  geom_ribbon(aes(ymin = nine, ymax = ten,  fill = "5000"), alpha='0.5') + 
  
  scale_fill_manual(name = "Buffer Size", breaks = c("500","5000"), 
                    values = c(
                      "500" = rgb(0.0, 1.0, 1.0) ,
                      "b" = rgb(0.0, .9, 1.0)  ,
                      "c" =rgb(0.0, 0.8, 1.0) ,
                      "d" =rgb(0.0, .7, 1.0) ,
                      "e" =rgb(0.0, .6, 1.0) ,
                      "f" =rgb(0.0, .5, 1.0) ,
                      "g" =rgb(0.0, .4, 1.0) ,
                      "h" =rgb(0.0, .3, 1.0) ,
                      "5000" = rgb(0.0, 0.2, 1.0))) + 
  xlab("Test Cycle") + ylab("Mean Squared Error") + 
  # legend themes
  theme(legend.text = element_text(size = 24)) +
  theme(axis.text=element_text(size=20),
        axis.title=element_text(size=28)) 




# Plot quality vs Sampling rate
data = loadQuality('movie-lens-100k/sampling/samples.txt')
df = data.frame(ind = 1:5001, one = data$one , two = data$two, 
                three = data$three , four = data$four, 
                five = data$five , six = data$six, 
                seven = data$seven , eight = data$eight, 
                nine = data$nine , ten = data$ten)

ggplot(data = df, aes(x = ind)) +
  geom_ribbon(aes(ymin = one, ymax = two), fill = rgb(0.0, 1.0, 1.0), alpha='0.5' ,  colour=NA) + 
  geom_ribbon(aes(ymin = two, ymax = three), fill = rgb(0.0, 0.85, 1.0), alpha='0.5',  colour=NA) + 
  geom_ribbon(aes(ymin = three, ymax = four), fill = rgb(0.0, 0.70, 1.0), alpha='0.5',  colour=NA) + 
  geom_ribbon(aes(ymin = four, ymax = five), fill = rgb(0.0, 0.65, 1.0), alpha='0.5',  colour=NA) + 
  geom_ribbon(aes(ymin = five, ymax = six), fill = rgb(0.0, 0.50, 1.0), alpha='0.5',  colour=NA) + 
  geom_ribbon(aes(ymin = six, ymax = seven), fill = rgb(0.0, 0.35, 1.0), alpha='0.5',  colour=NA) + 
  geom_ribbon(aes(ymin = seven, ymax = eight), fill = rgb(0.0, 0.20, 1.0), alpha='0.5',  colour=NA) + 
  geom_ribbon(aes(ymin = eight, ymax = nine), fill =rgb(0.0, 0.05, 1.0), alpha='0.5',  colour=NA) + 
  geom_ribbon(aes(ymin = nine, ymax = ten), fill = rgb(0.0, 0.0, 1.0), alpha='0.5',  colour=NA) + 
  #  geom_line(aes(y = one)) + 
  #  geom_line(aes(y = ten)) +
  scale_fill_manual(name ="Legend" , 
                    values = c(rgb(0.0, 1.0, 1.0), rgb(0.0, 1.0, 1.0),rgb(0.0, 1.0, 1.0),
                               rgb(0.0, 1.0, 1.0),
                               rgb(0.0, 1.0, 1.0),
                               rgb(0.0, 1.0, 1.0),rgb(0.0, 1.0, 1.0),rgb(0.0, 1.0, 1.0),rgb(0.0, 1.0, 1.0))) + 
  xlab("Test Cycle") + ylab("Mean Squared Error") + 
  # legend themes
  theme(legend.text = element_text(size = 24), legend.key = element_rect(colour = "transparent", fill = alpha('white', 0.0)) ,
        legend.position="bottom") +
  theme(axis.text=element_text(size=20),
        axis.title=element_text(size=28)) 









ggplot(data = df, aes(x = ind)) +
  geom_ribbon(aes(ymin = one, ymax = two, fill = "500"),  alpha='0.5') + 
  geom_ribbon(aes(ymin = two, ymax = three, fill =  "b" ), alpha='0.5') + 
  geom_ribbon(aes(ymin = three, ymax = four , fill = "c"), alpha='0.5') + 
  geom_ribbon(aes(ymin = four, ymax = five, fill ="d"), alpha='0.5' )+ 
  geom_ribbon(aes(ymin = five, ymax = six , fill = "e"), alpha='0.5') + 
  geom_ribbon(aes(ymin = six, ymax = seven, fill = "f"), alpha='0.5') + 
  geom_ribbon(aes(ymin = seven, ymax = eight , fill = "g"), alpha='0.5') + 
  geom_ribbon(aes(ymin = eight, ymax = nine, fill ="h"), alpha='0.5') + 
  geom_ribbon(aes(ymin = nine, ymax = ten,  fill = "5000"), alpha='0.5') + 
  
  scale_fill_manual(name = "Buffer Size", breaks = c("500","5000"), 
                    values = c(
                              "500" = rgb(0.0, 1.0, 1.0) ,
                              "b" = rgb(0.0, .9, 1.0)  ,
                               "c" =rgb(0.0, 0.8, 1.0) ,
                               "d" =rgb(0.0, .7, 1.0) ,
                               "e" =rgb(0.0, .6, 1.0) ,
                               "f" =rgb(0.0, .5, 1.0) ,
                               "g" =rgb(0.0, .4, 1.0) ,
                               "h" =rgb(0.0, .3, 1.0) ,
                               "5000" = rgb(0.0, 0.2, 1.0))) + 
  xlab("Test Cycle") + ylab("Mean Squared Error") + 
  # legend themes
  theme(legend.text = element_text(size = 24)) +
  theme(axis.text=element_text(size=20),
        axis.title=element_text(size=28)) 

