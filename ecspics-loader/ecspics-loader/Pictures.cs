using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using FileHelpers;

namespace ecspics_loader
{
    [DelimitedRecord(",")]
    public class Pictures
    {
        public string uid;
        public string photoId;
        public string fileName;
        public string originalUrl;
        public string thumbnailUrl;
        public int height;
        public int width;
        public double latitude;
        public double longitude;
        public int viewCount;
        public string license;
    }
}
