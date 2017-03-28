# l2v
Like2Vec implementation



# Dataset

Source:
> F. Maxwell Harper and Joseph A. Konstan. 2015. The MovieLens Datasets: History
and Context. ACM Transactions on Interactive Intelligent Systems (TiiS) 5, 4,
Article 19 (December 2015), 19 pages. DOI=http://dx.doi.org/10.1145/2827872


The original file "ratings.dat" and is in the following format:     
UserID::MovieID::Rating::Timestamp

- UserIDs range between 1 and 6040
- MovieIDs range between 1 and 3952
- Ratings are made on a 5-star scale (whole-star ratings only)
- Timestamp is represented in seconds since the epoch as returned by time(2)
- Each user has at least 20 ratings

# Log-Likelihood Ratio
To test Log-Likelihood ratio code, "ratings.dat" file had the timestamp removed with:

``` cat ratings.dat | awk -F  "::" '{print $1", " $2", " $3}' > ratings_filtered.dat```

"ratings_filtered.dat" is in the following format:       
**UserID, MovieID, Rating**
