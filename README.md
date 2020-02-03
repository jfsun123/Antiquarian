This repo uses Java to simulate and automatically trade using the ETrade API. 

## Simulations ##
There are many different algorithms that can be used to simulate stock trading on past data.  Historical data is scraped off the web, stored in a separate folder and then reread to calculate potential profits of each algorithm. 
The calculators for the following algorithms are included:
    ADX
    EMA
    Keltner Channel
    MACD
    Simple Moving Averages
    RSI
    
These calculators can be run through the src/historicalSimulator directory

## Automatic Trading ##
The trader works in a similar way to the simulator.  However, it needs a consumer key and secret put in Antiquarian.java to run through your ETrade account.  It is capable of trading using the algorithms listed above for the simulator and also has other features such as a stop loss.  These trades are done in real time and can automatically send emails to you when an event occurs.
